package com.android.example.lintjat;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClassTypeSpecific;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.java.JavaUClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.plaf.TextUI;

public class LogDetector extends Detector implements Detector.UastScanner {

    public static final Issue ISSUE = Issue.create(
            "LogUsage",
            "避免调用android.util.Log",
            "请勿直接调用android.util.Log，应该使用统一工具类",
            Category.LINT, 5, Severity.ERROR,
            new Implementation(LogDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("v", "d", "i", "w", "e", "wtf");
    }


    @Override
     public List<String> getApplicableCallNames(){
        return Arrays.asList("v", "d", "i", "w", "e", "wtf");
    }

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        ArrayList<Class<? extends UElement>> arrayList=new ArrayList<>();
        return super.getApplicableUastTypes();
    }

    @Override
    public void visitMethodCall(@NotNull JavaContext context,
                                @NotNull UCallExpression node,
                                @NotNull PsiMethod method) {
        if (context.getEvaluator().isMemberInClass(method, "android.util.Log")) {
            UElement uastParent = node.getUastParent();


//            method.deleteChildRange();

            while (true){
                //获取这个方法所咋的类， 注意区分
//                UCallExpression  uCallExpression;  这个是执行方法的时候
//                UMethod method1    这个是定义方法的时候
                if (uastParent!=null&&uastParent instanceof JavaUClass){
                    String name = ((JavaUClass) uastParent).getQualifiedName();
                    if (!TextUtils.isEmpty(name)&& name.contains("Utils")){
                        return;
                    }
                }else  if (uastParent==null){

                    break;
                }
                uastParent=uastParent.getUastParent();
            }
            context.report(ISSUE, node, context.getLocation(node), "避免调用android.util.Log");
        }
    }
}