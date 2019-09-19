package com.sollian.lintjar

import com.android.tools.lint.detector.api.*
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import org.jetbrains.uast.UCallExpression
import java.util.*

class BitmapFactoryDetector:Detector(), Detector.UastScanner {

    companion object {

        val ISSUE= Issue.create(
                "BitmapFactoryReplace",
                "BitmapFactoryReplace",
                "使用Glide或其他第三方框架代替BitmapFactory创建Bitmap",
                Category.CORRECTNESS,
                7,
                Severity.WARNING,
                Implementation(BitmapFactoryDetector::class.java,Scope.JAVA_FILE_SCOPE)
        )

    }

    override fun getApplicableMethodNames(): List<String>? {
        return Arrays.asList("decodeResource","decodeFile","decodeResourceStream","decodeByteArray","decodeStream",
                "decodeFileDescriptor")
    }

    override fun getApplicableCallNames(): List<String>? {
        return Arrays.asList("decodeResource","decodeFile","decodeResourceStream","decodeByteArray","decodeStream",
                "decodeFileDescriptor")
    }

    override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if(context.evaluator.isMemberInClass(method, "android.graphics.BitmapFactory")){

            context.report(ISSUE,context.getLocation(node),"使用Glide或其他第三方框架代替BitmapFactory创建Bitmap")
        }
    }

    override fun visitMethod(
            context: JavaContext,
            visitor: JavaElementVisitor?,
            call: PsiMethodCallExpression,
            method: PsiMethod
    ) {
        if(context.evaluator.isMemberInClass(method, "android.graphics.BitmapFactory")){

            context.report(ISSUE,context.getLocation(call),"使用Glide或其他第三方框架代替BitmapFactory创建Bitmap")
        }
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if(context.evaluator.isMemberInClass(method, "android.graphics.BitmapFactory")){

            context.report(ISSUE,context.getLocation(node),"使用Glide或其他第三方框架代替BitmapFactory创建Bitmap")
        }
    }
}