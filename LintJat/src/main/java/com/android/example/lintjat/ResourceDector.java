package com.android.example.lintjat;

import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNewExpression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;

import java.util.Arrays;
import java.util.List;

public class ResourceDector extends Detector implements Detector.XmlScanner {



    //    FilleUtils filleUtils=null;
    //LogUsage 是id是唯一的
    //参见 lint的xml文件
    public static final Issue ISSUE = Issue.create(
            "ResourceDector",
            "册是变量的名称",
            "不要乱起名字，敏感字不要使用",
            Category.SECURITY, 5, Severity.ERROR,
            new Implementation(TestDector.class, Scope.JAVA_FILE_SCOPE));


    @Override
    public boolean appliesTo(@NotNull ResourceFolderType folderType) {
        return super.appliesTo(folderType);
    }

    @Override
    public boolean appliesToResourceRefs() {

        UClass uClass;
        return super.appliesToResourceRefs();

    }
}
