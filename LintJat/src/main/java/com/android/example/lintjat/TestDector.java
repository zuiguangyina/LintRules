package com.android.example.lintjat;

import com.android.ddmlib.Log;
import com.android.example.lintjat.Utils.CommandUtils;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.client.api.LintDriver;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UCallableReferenceExpression;
import org.jetbrains.uast.UCatchClause;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UClassTypeSpecific;
import org.jetbrains.uast.UElement;

import org.jetbrains.uast.UField;
import org.jetbrains.uast.ULocalVariable;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.UReferenceExpression;
import org.jetbrains.uast.USimpleNameReferenceExpression;
import org.jetbrains.uast.UTypeReferenceExpression;
import org.jetbrains.uast.java.JavaULiteralExpression;
import org.jetbrains.uast.java.JavaUQualifiedReferenceExpression;
import org.jetbrains.uast.kotlin.KotlinUEnumConstant;
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression;

import org.jetbrains.uast.kotlin.KotlinUObjectLiteralExpression;
import org.jetbrains.uast.kotlin.KotlinUQualifiedReferenceExpression;
import org.jetbrains.uast.kotlin.KotlinUSimpleReferenceExpression;
import org.jetbrains.uast.kotlin.KotlinUVarargExpression;
import org.jetbrains.uast.kotlin.expressions.KotlinUCollectionLiteralExpression;
import org.jetbrains.uast.visitor.AbstractUastVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class TestDector extends Detector implements Detector.UastScanner {

public  int initial=0;

//    FilleUtils filleUtils=null;
    //LogUsage 是id是唯一的
    //参见 lint的xml文件
    public static final Issue ISSUE = Issue.create(
            "SayHelloA",
            "册是变量的名称",
            "不要乱起名字，敏感字不要使用",
            Category.SECURITY, 5, Severity.ERROR,
            new Implementation(TestDector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public void beforeCheckFile(@NotNull Context context) {

//        //这个就是我们的任务啊
//        Project project = context.getProject();
//        LintDriver driver = context.getDriver();
//        Project mainProject = context.getMainProject();
//        LintClient client = context.getProject().getClient();
//        client.getSdk();
////        project.getDir();

        //将要检查的文件
        String absolutePath = context.file.getAbsolutePath();

        super.beforeCheckFile(context);

    }

    @Override
    public void afterCheckFile(@NotNull Context context) {
        super.afterCheckFile(context);
        //检查文件之后
    }

    @Override
    public void beforeCheckEachProject(@NotNull Context context) {
        super.beforeCheckEachProject(context);

    }



    @Override
    public void beforeCheckRootProject(@NotNull Context context) {
        super.beforeCheckRootProject(context);

        if (initial==0){
            Collection<File> versionFileList = CommandUtils.getVersionFileList(context.getProject());
            System.out.println("文件数量"+versionFileList.size());
            for (File gitFile:versionFileList){
                System.out.println(gitFile.getAbsoluteFile());
            }
            initial++;
        }

    }


    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
//        此方法返回需要检查的AST节点的类型，类型匹配的UElement将会被createUastHandler(createJavaVisitor)
//        创建的UElementHandler(Visitor)检查
        //用于检测硬编码
        List< Class<? extends UElement>> classes =new ArrayList<>();
        classes.add(UTypeReferenceExpression.class);
//        classes.add(UQualifiedReferenceExpression.class);
//        classes.add(KotlinUQualifiedReferenceExpression.class);
        return classes;
        //return Collections.<Class<? extends UElement>>singletonList(USimpleNameReferenceExpression.class,);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull final JavaContext context) {
        final NamingConventionVisitor visitor=new NamingConventionVisitor(context);
//        filleUtils=new FilleUtils(context.getMainProject());
        return new UElementHandler(){

            @Override
            public void visitMethod(@NotNull UMethod node) {
                //定义的方法包含构造方法,对于定义成员变量是无效的
//                node.accept(visitor);

//                PsiMethod psiMethod;
            }
//
//            @Override
            public void visitClass(@NotNull UClass node) {
                //定义类的地方
//                node.accept(visitor);


            }

            @Override
            public void visitCallExpression(@NotNull UCallExpression node) {
                //执行方法的时候，也就是调用的时候
//                 node.accept(visitor);
            }
//
            @Override
            public void visitField(@NotNull UField node) {
                //类的属性
//                super.visitField(node);
            }

            @Override
            public void visitLocalVariable(@NotNull ULocalVariable node) {
                //局部变量，
//                node.accept(visitor);
//                super.visitLocalVariable(node);
            }
//
            @Override
            public void visitSimpleNameReferenceExpression(@NotNull USimpleNameReferenceExpression node) {
                //通过引用调用功能函数 也就是 obj.methond()的方式，但是不包含this.method
                //
//                super.visitSimpleNameReferenceExpression(node);
//                node.accept(visitor);
            }
//
            @Override
            public void visitCallableReferenceExpression(@NotNull UCallableReferenceExpression node) {
//                super.visitCallableReferenceExpression(node);
                //主要针对kotlin与java8的例如 var name=obj::method
//                node.accept(visitor);

            }

            @Override
            public void visitQualifiedReferenceExpression(@NotNull UQualifiedReferenceExpression node) {

//                super.visitQualifiedReferenceExpression(node);
//                node.accept(visitor);
            }

            @Override
            public void visitTypeReferenceExpression(@NotNull UTypeReferenceExpression node) {
//                super.visitTypeReferenceExpression(node);
//                node.accept(visitor);
            }
        };
    }

//    定义一个继承自AbstractUastVisitor的访问器，用来处理感兴趣的问题
    public  class NamingConventionVisitor extends AbstractUastVisitor {

        JavaContext context;

//        UClass uClass;

        public NamingConventionVisitor(JavaContext context) {
            this.context = context;
//            this.uClass = uClass;
        }

    @Override
    public boolean visitTypeReferenceExpression(@NotNull UTypeReferenceExpression node) {
        context.report(ISSUE, node, context.getLocation(node),"UTypeReferenceExpression");
        return true;
    }

    @Override
        public boolean visitLocalVariable(@NotNull ULocalVariable node) {

            context.report(ISSUE, (PsiElement) node, context.getLocation(node.getIdentifyingElement()),"LocalVariable");
            return  true;

//            return super.visitLocalVariable(node);
        }

        @Override
        public boolean visitClass(@NotNull UClass node) {
            return super.visitClass(node);
        }
//
        @Override
        public boolean visitMethod(@NotNull UMethod node) {
            context.report(ISSUE, node, context.getLocation(node), "UCallExpression");



            return  true;

        }

        @Override
        public boolean visitCallExpression(@NotNull UCallExpression node) {
            // 定义函数不会报错，但是其余的new  对象也就是调用构造函数会报错
            //其余的任何函数的调用都会报错，基本运算不会报错
//            context.report(ISSUE, node, context.getLocation(node), "UCallExpression");
//            return  true;
            return super.visitCallExpression(node);
        }

        @Override
        public boolean visitSimpleNameReferenceExpression(@NotNull USimpleNameReferenceExpression node) {
            // 实现了UReferenceExpression
            context.report(ISSUE, node, context.getLocation(node), "避免调用visitSimpleNameReferenceExpression");
            return true;
        }
//
        @Override
        public boolean visitCallableReferenceExpression(@NotNull UCallableReferenceExpression node) {
            context.report(ISSUE, node, context.getLocation(node), "visitCallableReferenceExpression");
            return  true;
        }

         @Override
        public boolean visitQualifiedReferenceExpression(@NotNull UQualifiedReferenceExpression node) {
                 context.report(ISSUE, node, context.getLocation(node), "UQualifiedReferenceExpression");
             return  true;
    }

}
//    @Nullable
//    @Override
//    public List<String> getApplicableReferenceNames() {
//        // 这个要与Constructor区别，
//        // 创建logUtils 的时候不会检查
//        //必须是logUtils.xxxxx的时候才会检车，也就是只有在使用的时候才会检查
//        // 不支持正则表达式
//        //对于lambda 方法的引用无效
//        return Arrays.asList("test");
//    }
//
//
//
//    @Override
//    public void visitReference(JavaContext context, UReferenceExpression reference,  PsiElement referenced) {
//        String resolvedName = reference.getResolvedName();
//        System.out.println("name"+resolvedName);
//        context.report(ISSUE, reference, context.getLocation(reference), "避免调用android.util.Log");
//
//    }



//    @Nullable
//    @Override
//    public Collection<String> getApplicableElements() {
//        //这个是xml分析的时候有用的，返回的是标签名称
//        //
//        return super.getApplicableElements();
//    }
//
//    @Nullable
//    @Override
//    public Collection<String> getApplicableAttributes() {
//        //这个是返回的标签的属性值，跟上面的标签名都是分析处理xml文件的
//        return super.getApplicableAttributes();
//    }
    //    @Nullable
//    @Override
//    public List<String> getApplicableConstructorTypes() {
//        // 检查构造函数,  只有调用构造函数的时候才会检查，不是检查构造函数本事，而是检测调用的调用
//        //也就是声明对象的地方
//        return Arrays.asList("com.sollian.customlintrules.utils.LogUtils");
//    }
//
//    @Override
//    public void visitConstructor(@NotNull JavaContext context, @NotNull UCallExpression node, @NotNull PsiMethod constructor) {
////        String resolvedName = reference.getResolvedName();
//        context.report(ISSUE, node, context.getLocation(node), "不要乱用");
//    }



//    @Override
//    public List<String> getApplicableMethodNames() {
//        //返回的列表代表函数的名字
//        //这些函数一旦被程序调用话会执行visitMethodCall 回调方法
//        //对于定义函数无效.
//        return Arrays.asList("test", "hanlei", "i", "w", "e", "wtf");
//    }
//    @Override
//    public void visitMethodCall(@NotNull JavaContext context,
//                                @NotNull UCallExpression node,
//                                @NotNull PsiMethod method) {
//
//        context.report(ISSUE, node, context.getLocation(node), "避免调用visitMethodCall");
//    }

//    @Override
//    public List<String> getApplicableCallNames(){
//        return Arrays.asList("test", "hanlei", "i", "w", "e", "wtf");
//    }
//
//    @Override
//    public void visitMethod(@NotNull JavaContext context, @Nullable JavaElementVisitor visitor, @NotNull PsiMethodCallExpression call, @NotNull PsiMethod method) {
////        super.visitMethod(context, visitor, call, method);
//        context.report(ISSUE, call, context.getLocation(call), "避免调用android.util.Log");
//    }
////
//    @Override
//    public void visitMethod(@NotNull JavaContext context, @NotNull UCallExpression node, @NotNull PsiMethod method) {
////        super.visitMethod(context, node, method);
//        context.report(ISSUE, node, context.getLocation(node), "避免调用android.util.Log");
//    }

    //    @Nullable
//    @Override
//    public List<String> getApplicableReferenceNames() {
//        //这个是引用的名字
////        return super.getApplicableReferenceNames();
//
//        return Arrays.asList("test", "callable", "a", "ab", "e", "wtf","LogUtils");
//    }
//
//    @Override
//    public void visitReference(@NotNull JavaContext context, @NotNull UReferenceExpression reference, @NotNull PsiElement referenced) {
//        super.visitReference(context, reference, referenced);
//        context.report(ISSUE, reference, context.getLocation(reference), "visitReference");
//    }

}
