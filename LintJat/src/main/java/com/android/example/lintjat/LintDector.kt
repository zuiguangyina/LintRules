package com.sollian.lintjar

import com.android.tools.lint.detector.api.*

class  LintDector  : Detector(),Detector.UastScanner{

    companion object{
        internal var ISSUE = Issue.create(
                "ResourceDector",
                "册是变量的名称",
                "不要乱起名字，敏感字不要使用",
                Category.SECURITY, 5, Severity.ERROR,
                Implementation(LintDector::class.java, Scope.JAVA_FILE_SCOPE))
    }


}