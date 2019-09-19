package com.android.example.lintjat;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class ToastUtilsDetector extends Detector implements Detector.UastScanner {

    private static final Class<? extends Detector> DETECTOR_CLASS = ToastUtilsDetector.class;
    private static final EnumSet<Scope> DETECTOR_SCOPE = Scope.JAVA_FILE_SCOPE;
    private static final Implementation IMPLEMENTATION = new Implementation(
            DETECTOR_CLASS,
            DETECTOR_SCOPE
    );

    private static final String ISSUE_ID = "ToastUseError";
    private static final String ISSUE_DESCRIPTION = "You should use our{ToastUtils}";
    private static final String ISSUE_EXPLANATION = "You should NOT use android.widget.Toast directly. Instead you should use ToastUtils we offered.";
    private static final Category ISSUE_CATEGORY = Category.CORRECTNESS;
    private static final int ISSUE_PRIORITY = 9;
    private static final Severity ISSUE_SEVERITY = Severity.ERROR;
    private static final String CHECK_PACKAGE = "android.widget.Toast";

    public static final Issue ISSUE = Issue.create(
            ISSUE_ID,
            ISSUE_DESCRIPTION,
            ISSUE_EXPLANATION,
            ISSUE_CATEGORY,
            ISSUE_PRIORITY,
            ISSUE_SEVERITY,
            IMPLEMENTATION
    );

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("makeText", "show");
    }

    @Override
    public void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression node, @NotNull PsiMethod method) {
        if (!context.getEvaluator().isMemberInClass(method, CHECK_PACKAGE)) {
            return;
        }

        List<UExpression> args = node.getValueArguments();
        UExpression duration = null;
        if (args.size() == 3) {
            duration = args.get(2);
        }
        LintFix fix = null;
        if (duration != null) {
            String replace;
            if ("Toast.LENGTH_LONG".equals(duration.toString())) {
                replace = "ToastUtils.showLong(" + args.get(0).toString() + ", " + args.get(1).toString() + ");";
            } else {
                replace = "ToastUtils.showShort(" + args.get(0).toString() + ", " + args.get(1).toString() + ");";
            }
            fix = fix().name("Replace with ToastUtils")
                    .replace()
                    .with(replace)
                    .build();
        }
        if (fix != null) {
            context.report(ISSUE, node, context.getLocation(node), ISSUE_DESCRIPTION, fix);
        }
    }

}
