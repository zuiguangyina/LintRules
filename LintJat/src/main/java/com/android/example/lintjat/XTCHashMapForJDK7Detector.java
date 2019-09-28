package com.android.example.lintjat;

import com.android.SdkConstants;
import com.android.example.lintjat.Utils.PsiUtils;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTypeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UDeclarationsExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.ULocalVariable;
import org.jetbrains.uast.visitor.AbstractUastVisitor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 针对 JDK7 新语法的加强检测
 * http://docs.oracle.com/javase/7/docs/technotes/guides/language/type-inference-generic-instance-creation.html
 * </p>
 * created by OuyangPeng at 2017/9/5 16:04
 */
public class XTCHashMapForJDK7Detector extends Detector implements Detector.UastScanner {

    private static final Class<? extends Detector> DETECTOR_CLASS = XTCHashMapForJDK7Detector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "XTC_HashMapForJDK7";
    private static final String ISSUE_DESCRIPTION = "警告:为了更好的性能，请使用 {SparseArray} 来替代 {HashMap}";
    private static final String ISSUE_EXPLANATION = "如果map类型的key值为 Integer类型，使用Android 特有的API `SparseArray`。" +
            "这个检查确定的情况下，为了更好的性能，你可能要考虑使用`SparseArray`代替`HashMap`。" +
            "当你的key类型是int等原始类型的时候，你可以使用`SparseIntArray`来避免自动装箱将`int` 转换为 `Integer`。";

    private static final Category ISSUE_CATEGORY = Category.PERFORMANCE;
    private static final int ISSUE_PRIORITY = 4;
    private static final Severity ISSUE_SEVERITY = Severity.WARNING;

    /** Using HashMaps where SparseArray would be better */
    public static final Issue ISSUE = Issue.create(
            ISSUE_ID,
            ISSUE_DESCRIPTION,
            ISSUE_EXPLANATION,
            ISSUE_CATEGORY ,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    ).addMoreInfo("http://blog.csdn.net/u010687392/article/details/47809295").addMoreInfo("http://www.cnblogs.com/CoolRandy/p/4547904.html");



    private static final String INTEGER = "Integer";
    private static final String BOOLEAN = "Boolean";
    private static final String BYTE = "Byte";
    private static final String LONG = "Long";
    private static final String HASH_MAP = "HashMap";

    //匹配的是 HashMap<A,B>等
    private static final Pattern PATTERN = Pattern.compile(".*<(.*),(.*)>");


    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        ArrayList <Class<? extends UElement>> arrayList=new ArrayList<>();
        //局部变量
        arrayList.add(UDeclarationsExpression.class);
        //成员变量
        arrayList.add(UField.class);
        return arrayList;
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull JavaContext context) {
        final MyHandler handler=new MyHandler(context);
        return  new UElementHandler(){
            @Override
            public void visitLocalVariable(@NotNull ULocalVariable node) {
                 node.accept(handler);
            }

            @Override
            public void visitField(@NotNull UField node) {
                super.visitField(node);
                node.accept(handler);
            }
        };
    }

    private  class  MyHandler extends AbstractUastVisitor {
        JavaContext context;

        public MyHandler(JavaContext context) {
            this.context = context;
        }

        @Override
        public boolean visitLocalVariable(@NotNull ULocalVariable node) {
            PsiElement mapType = PsiUtils.getChildForWidth(node.getJavaPsi(), PsiTypeElement.class);
            if (mapType!=null){
                String text = mapType.getText();
                // 判断是不是 HashMap类型的
                if (text.startsWith("HashMap")){
                    checkHashMap(context,node,text);
                }
            }
            return super.visitLocalVariable(node);
        }

        @Override
        public boolean visitField(@NotNull UField node) {
            PsiElement childForWidth = PsiUtils.getChildForWidth(node.getSourcePsi(), PsiTypeElement.class);
            if (childForWidth!=null){
                String text = childForWidth.getText();
                // 判断是不是 HashMap类型的
                if (text.startsWith("HashMap")){
                    checkHashMap(context,node.getPsi(),text);
                }
            }
            return super.visitField(node);
        }
    }

    private void checkHashMap(JavaContext context, PsiElement node,String fullName) {

            /*
            JDK7 新写法
            HashMap<Integer, String> map2 = new HashMap<>();
            map2.put(1, "name");
            Map<Integer, String> map3 = new HashMap<>();
            map3.put(1, "name");
             */

            checkCore2(context,node,fullName);

    }
    //没有处理泛型的嵌套，只是处理最简单的类型。
    private void checkCore2(JavaContext context,PsiElement node, String fullTypeName) {
        Matcher m = PATTERN.matcher(fullTypeName);
        if (m.find()) {
            String typeName = m.group(1).trim();
            String valueType = m.group(2).trim();
            int minSdk = context.getMainProject().getMinSdk();
//            System.out.println("XTCHashMapForJDK7Detector checkCore2() 出现lint检测项，对应的责任人为： " + relativePersonName);
            String appendMessage = " ,请速度修改";
            if (typeName.equals(INTEGER) || typeName.equals(BYTE)) {
                if (valueType.equals(INTEGER)) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseIntArray(...) } 来替代 {HashMap}" + appendMessage);
                } else if (valueType.equals(LONG) && minSdk >= 18) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseLongArray(...) } 来替代 {HashMap}" + appendMessage);
                } else if (valueType.equals(BOOLEAN)) {
                    context.report(ISSUE, node, context.getLocation(node),
                            "为了更好的性能，请使用 {SparseBooleanArray(...) } 来替代 {HashMap}" + appendMessage);
                } else {
                    String message =  String.format(
                            "为了更好的性能，请使用 {SparseArray<%1$s>(...) } 来替代 {HashMap}",
                            valueType);
                    context.report(ISSUE, node, context.getLocation(node),message + appendMessage);
                }
            } else if (typeName.equals(LONG) && (minSdk >= 16 ||
                    Boolean.TRUE.equals( context.getMainProject().dependsOn(
                            SdkConstants.SUPPORT_LIB_ARTIFACT)))) {
                boolean useBuiltin = minSdk >= 16;
                String message = useBuiltin ?
                        "为了更好的性能，请使用 {LongSparseArray(...) } 来替代 {HashMap}" + appendMessage:
                        "为了更好的性能，请使用 {android.support.v4.util.LongSparseArray(...) } 来替代 {HashMap}" + appendMessage;
                context.report(ISSUE, node, context.getLocation(node),
                        message);
            }
        }
    }
}