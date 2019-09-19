package com.android.example.lintjat;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

//感觉跟写Java业务代码很像， 只需要了解API功能就可以快速上手。
// 自定义Lint有个总入口IssueRegistry类，在List里返回需要检查的自定义规则即可。
public class CustomIssueRegistry extends IssueRegistry {
    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }

    @NotNull
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
//                LogDetector.ISSUE
//                 NewThreadDetector.ISSUE
//                , ConcurrentModifyDetector.ISSUE
//                , ModuleAccessibleDetector.ISSUE
//                , DrawableAttrDetector.ISSUE
//                    PrintStackTraceDetector.ISSUE
//                , LinearLayoutManagerDetector.ISSUE
//                , PopupWindowDetector.ISSUE
//                , AttrPrefixDetector.ISSUE
//                    TestDector.ISSUE
//                IntentExtraKeyDetector.ISSUE
                XTCCloseDetector.ISSUE
        );
    }
}
