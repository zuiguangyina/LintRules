package com.android.example.lintjat;
import com.android.tools.lint.client.api.UastParser;
import com.android.tools.lint.detector.api.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UAnonymousClass;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UField;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class SerializableDetector extends Detector implements Detector.UastScanner {

    private static final String CLASS_SERIALIZABLE = "java.io.Serializable";

    private String[] basicTypes = {"byte", "short", "int", "long", "float", "double",
            "char", "boolean", "byte[]", "short[]", "int[]", "long[]", "float[]", "double[]",
            "char[]", "boolean[]","java.lang.String", "java.lang.Double",
            "java.lang.Boolean", "java.lang.Long", "java.lang.Short",
            "java.lang.Integer", "java.lang.Char", "java.lang.Boolean","java.lang.String[]",
            "java.lang.Double[]", "java.lang.Boolean[]", "java.lang.Long[]", "java.lang.Short[]",
            "java.lang.Integer[]", "java.lang.Char[]", "java.lang.Boolean[]"};

    private static HashSet<String> hashSet = new HashSet<>();

    public static final Issue ISSUE = Issue.create(
            "ClassSerializable",
            "Bean类成员需要实现Serializable接口",
            "Bean类成员需要实现Serializable接口",
            Category.SECURITY, 5, Severity.ERROR,
            new Implementation(SerializableDetector.class, Scope.JAVA_FILE_SCOPE));

    @Nullable
    @Override
    public List<String> applicableSuperClasses() {
        //父类是"java.io.Serializable"
        //可以获取指定类的全部子类
        return Collections.singletonList(CLASS_SERIALIZABLE);
    }

    @Override
    public void visitClass(JavaContext context, UClass declaration) {
        if (declaration instanceof UAnonymousClass) {
            return;
        }
        sortClass(context, declaration);
    }

    //递归直到基本数据类型
    private void sortClass(JavaContext context, UClass declaration) {
        if (hashSet.contains(declaration.getPsi().getQualifiedName())) {
            //参考动态规划的备忘录方式，计算出结果的类不再计算第二遍
            //System.out.println(declaration.getPsi().getQualifiedName() + "已经被过滤");
            return;
        }

        UastParser parser = context.getClient().getUastParser(context.getProject());
        boolean isSerialized = false;
        for (PsiClassType psiClassType : declaration.getImplementsListTypes()) {
            if (CLASS_SERIALIZABLE.equals(psiClassType.getCanonicalText())) {
                //实现了序列化
                isSerialized = true;
                break;
            }
        }
        //System.out.println("++++++" + declaration.getPsi().getQualifiedName());
        for (String type : basicTypes) {
            if (type.equalsIgnoreCase(declaration.getPsi().getQualifiedName())) {
                //基本数据类似不需要实现Serializable，继续判断其它成员变量
                return;
            }
        }

        if (!isSerialized) {
            if (!hashSet.contains(declaration.getPsi().getQualifiedName())) {
                context.report(ISSUE,
                        declaration.getNameIdentifier(),
                        context.getLocation(declaration.getNameIdentifier()),
                        String.format("成员变量 `%1$s` 需要实现Serializable接口",
                                declaration.getPsi().getQualifiedName()));
                hashSet.add(declaration.getPsi().getQualifiedName());
                System.out.println("size" + hashSet.size() + "/" + declaration.getPsi().getQualifiedName() + "没实现序列化");
            }
            return;
        }

        //检查内部类
        for (UClass uClass : declaration.getInnerClasses()) {
            //递归判断内部类, 查看成员参数是否实现了序列化方法
            sortClass(context, uClass);
        }

        //检查成员变量
        for (UField uField : declaration.getFields()) {
            boolean isBasic = false;
            for (String type : basicTypes) {
                if (type.equalsIgnoreCase(uField.getType().getCanonicalText())) {
                    //基本数据类似不需要实现Serializable，继续判断其它成员变量
                    isBasic = true;
                    break;
                }
            }

            if (isBasic) {
                //如果是基本数据类型继续循环
                continue;
            }

            if (uField.getType().getCanonicalText()
                    .matches("^[A-Za-z0-9.]*[List|Set]<[A-Za-z0-9.]*>$")) {
                //使用了泛型则判断泛型是否实现了Serializable
                String genericType = uField.getType().getCanonicalText().substring(
                        uField.getType().getCanonicalText().indexOf("<") + 1,
                        uField.getType().getCanonicalText().indexOf(">"));

                PsiClass cls = parser.getEvaluator().findClass(genericType);
                UClass uGeneric = context.getUastContext().getClass(cls);
                sortClass(context, uGeneric);
            } else {
                PsiClass psiClass = parser.getEvaluator()
                        .findClass(uField.getType().getCanonicalText());
                sortClass(context, context.getUastContext().getClass(psiClass));
            }
        }
    }
}
