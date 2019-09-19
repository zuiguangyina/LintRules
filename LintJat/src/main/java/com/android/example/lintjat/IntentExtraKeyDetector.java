package com.android.example.lintjat;

import com.android.annotations.NonNull;
import com.android.ddmlib.Log;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import com.android.tools.lint.detector.api.UastLintUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.PsiClassReferenceType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin;
import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UComment;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UForExpression;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UReferenceExpression;
import org.jetbrains.uast.USimpleNameReferenceExpression;
import org.jetbrains.uast.UVariable;
import org.jetbrains.uast.UWhileExpression;
import org.jetbrains.uast.UastLiteralUtils;
import org.jetbrains.uast.UastUtils;
import org.jetbrains.uast.java.JavaUSimpleNameReferenceExpression;
import org.jetbrains.uast.util.UastExpressionUtils;
import org.jetbrains.uast.values.UValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class IntentExtraKeyDetector extends Detector implements Detector.UastScanner {


    private  static FilleUtils filleUtils;
    public static final Issue ISSUE = Issue.create(
            "extraKey",
            "please avoid use hardcode defined intent extra key",
            "defined in another activity",
            Category.SECURITY, 5, Severity.ERROR,
            new Implementation(IntentExtraKeyDetector.class, Scope.JAVA_FILE_SCOPE));


    public IntentExtraKeyDetector() {
    }




    // ---- Implements JavaScanner ----

    @Override
    public List<String> getApplicableMethodNames() {
        return Collections.singletonList("putExtra");
    }


    private HashMap<String,String> hashMap=new LinkedHashMap<>(8);
    private  void ensureExtraKey(JavaContext context, @NonNull UCallExpression node) {
        List<UExpression> valueArguments = node.getValueArguments();
        if (valueArguments.size() != 2) {
            return;
        }
        UExpression expression = valueArguments.get(0);
        //当第一个参数值类型为String,这样是硬编码
        if (expression instanceof ULiteralExpression) {
            if (((ULiteralExpression) expression).isString()){
                context.report(ISSUE, node, context.getLocation(node), "please avoid use hardcode defined Intent.putExtra key");
                return;
            }

        }
        if (expression instanceof USimpleNameReferenceExpression) {
            //获取该变量的定义name
            USimpleNameReferenceExpression referenceExpression= (USimpleNameReferenceExpression) expression;
            String targetName=referenceExpression.getIdentifier();
            //是个空值，不知道为什么，有什么用。
            PsiElement javaPsi = referenceExpression.getJavaPsi();
            //PsiReferenceExpressionImpl
            hashMap.put("javaPsi",javaPsi.getClass().getName());
            PsiExpression variableDefine = getVariableDefine(javaPsi, targetName);
            if (variableDefine!=null){
                if (variableDefine instanceof PsiReference){
                    context.report(ISSUE, node, context.getLocation(node), "please defined intent extra key start with EXTRA_");
                }else if (variableDefine instanceof PsiLiteralExpression){
                    PsiLiteralExpression literalExpression= (PsiLiteralExpression) variableDefine;
                    String text = literalExpression.getText();
                    hashMap.put("literString",text);
                }
            }
//            hashMap.put("QualifiedName",targetName);
            if (!targetName.startsWith("EXTRA_")) {
                context.report(ISSUE, node, context.getLocation(node), "please defined intent extra key start with EXTRA_");
            }
        }

    }

    @Override
    public void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression node, @NotNull PsiMethod method) {
        if (filleUtils==null){
            filleUtils=new FilleUtils(context.getMainProject());
        }
//        UMethod containingUMethod = UastUtils.getContainingUMethod(node);
//        if (containingUMethod!=null){
//            PsiElement[] children = containingUMethod.getChildren();
//            if (children!=null&&children.length!=0){
//                PsiElement psiElement=children[children.length-1];
//                if (psiElement instanceof  PsiCodeBlock){
//                    PsiCodeBlock psiCodeBlock= (PsiCodeBlock) psiElement;
//                    PsiElement[] childrenTwo = psiCodeBlock.getChildren();
//                    if (childrenTwo!=null&&childrenTwo.length!=0){
//                        for ( int pos=0;pos<childrenTwo.length;pos++){
//                            hashMap.put("pos"+pos,childrenTwo[pos].getClass().getName());
//                            hashMap.put("con"+pos,childrenTwo[pos].getText());
//                        }
//                    }
//                }
//            }
//        }
//

        //body 可能是null
//        String body = method.getText();
//        if (body!=null){
//            hashMap.put("text",body);
//        }

        //这个是什么玩意


//
//        List<PsiType> typeArguments = node.getTypeArguments();
//        if (typeArguments!=null&&typeArguments.size()!=0){
//            for (int i=0;i<typeArguments.size();i++){
//                PsiType psiType = typeArguments.get(i);
//                hashMap.put("psiText"+i,psiType.getCanonicalText());
//                hashMap.put("psiClass"+i,psiType.getClass().getName());
//            }
//        }else {
//            if (typeArguments==null){
//                hashMap.put("psiText","null");
//            }else if (typeArguments.size()==0){
//                hashMap.put("psiText","size=0");
//            }
//        }
//
//
//        PsiElement[] children = method.getChildren();
//        if (children!=null&&children.length!=0){
//            for (int pos=0;pos<children.length;pos++){
//                PsiElement element=children[pos];
//                hashMap.put("child"+pos,element.getText());
//            }
//        }
//        PsiCodeBlock body = method.getBody();
//        if (body!=null){
//            hashMap.put("body",body.getText());
//        }else {
//            hashMap.put("body","null");
//        }
//        context.report(ISSUE, node, context.getLocation(node), "please defined intent extra key start with EXTRA_");



        //        List<UExpression> valueArguments = node.getValueArguments();
//        if (valueArguments!=null&&valueArguments.size()!=0){
////            for (int i=0;i<valueArguments.size();i++){
////                UExpression uExpression = valueArguments.get(i);
////                hashMap.put("valueName"+i,uExpression.asRenderString());
////            }
////        }
//

//        if (context.getEvaluator().isMemberInClass(method, "android.content.Intent")) {
//            if (node.getValueArgumentCount()==2){
//                ensureExtraKey(context, node);
//            }
//            return;
//        }

            // 获取PsiElement 的父类
//        PsiElement parent = method.getParent();
//        //  PsiClass containingClass = method.getContainingClass();  可以使用这个获取更方便
//        if (parent!=null && parent instanceof  PsiClass){
//            PsiClass psiClass= (PsiClass) parent;
//            String qualifiedName = psiClass.getQualifiedName();
//            //qualifiedName:android.content.Intent  可以获取到这个类
//            hashMap.put("qualifiedName",qualifiedName);
//        }else {
//            if (parent==null){
//                hashMap.put("qualifiedName","parent=null");
//            }else {
//                hashMap.put("qualifiedName","parent 不是类");
//            }
//        }
        UExpression receiver = node.getReceiver();
        if (receiver instanceof  USimpleNameReferenceExpression){
            USimpleNameReferenceExpression  uSimpleNameReferenceExpression= (USimpleNameReferenceExpression) receiver;
            //receiver:org.jetbrains.uast.java.JavaUSimpleNameReferenceExpression
            hashMap.put("receiver",receiver.getClass().getName());
            PsiElement resolve = uSimpleNameReferenceExpression.resolve();
            //PsiLocalVariable 根据引用查询到定义这个变量的地方
            hashMap.put("resolve",resolve.getClass().getName());
            if (resolve instanceof PsiLocalVariable){
                PsiLocalVariable localVariable = (PsiLocalVariable) resolve;
                //获取这个变量的类型
                PsiTypeElement typeElement = localVariable.getTypeElement();
                //typeText:Intent
                hashMap.put("typeText",typeElement.getText());
                //type:com.intellij.psi.impl.source.PsiClassReferenceType
                hashMap.put("type",typeElement.getType().getClass().getName());

                PsiClassReferenceType  referenceType= (PsiClassReferenceType) typeElement.getType();
                //获取到这个这个引用指向的类
                PsiClass resolve1 = referenceType.resolve();
                //typeClass:android.content.Intent
                hashMap.put("typeClass",resolve1.getQualifiedName());
            }
        }

        context.report(ISSUE, node, context.getLocation(node), "please defined intent extra key start with EXTRA_");
    }

    private  PsiExpression getVariableDefine(PsiElement sourcePsi,String name){
//        PsiElement sourcePsi1 = uExpression.getSourcePsi();
        PsiElement sourcePsi1 = sourcePsi;
        if (sourcePsi1==null)
            return null;
        PsiElement prevSibling = sourcePsi1.getPrevSibling();
        while (prevSibling!=null){
            hashMap.put("prevSibling"+prevSibling,prevSibling.getText());
            if (prevSibling instanceof PsiDeclarationStatement){
                PsiDeclarationStatement psiDeclarationStatement= (PsiDeclarationStatement) prevSibling;
                PsiElement declaredElement = psiDeclarationStatement.getDeclaredElements()[0];
                if (declaredElement!=null&&declaredElement instanceof PsiLocalVariable){
                    PsiLocalVariable localVariable= (PsiLocalVariable) declaredElement;
                    String text = localVariable.getIdentifyingElement().getText();
                    if (text!=null&&text.equals(name)){
                        //初始化的数值，可能是个数字，字符串，也可能是个引用。
                        PsiExpression initializer = localVariable.getInitializer();
                        return initializer;
                    }
                }
            }
            prevSibling=prevSibling.getPrevSibling();
        }
        PsiElement parent = sourcePsi1.getParent();

        if (parent!=null&&parent instanceof PsiMethod){
            //没有找到局部变量
            PsiElement psiFiledFromVariable = getPsiFiledFromVariable(name, parent);
            if (psiFiledFromVariable instanceof  PsiExpression){
                //返回
                return (PsiExpression) psiFiledFromVariable;
            }
        }else if (parent!=null){
//            sourcePsi1=parent.getPrevSibling();
            hashMap.put("parent"+parent,parent.getText());
            //递归查询
            return  getVariableDefine(parent,name);
        }
        hashMap.put("parent","null");
        return   null;
    }

    public  PsiElement  getPsiFiledFromVariable(String name,PsiElement psiElement){
        PsiElement parent = psiElement;
         while (parent!=null){
             if (parent instanceof  PsiClass){
                 PsiClass psiClass= (PsiClass) parent;
                 PsiField[] allFields = psiClass.getAllFields();
                 if (allFields!=null){
                     for ( int pos=0;pos<allFields.length;pos++){
                         PsiField psiField= allFields[pos];
                         PsiElement[] children = psiField.getChildren();
                         if (children!=null){
                             boolean find=false;
                             for (int child=0;child<children.length;child++){
                                 PsiElement childElement=children[child];
                                 //psi 不支持kotlin吗
                                 //找到filed的名字与name一样的了。
                                 if (childElement instanceof PsiJavaToken &&find){
                                     childElement=childElement.getNextSibling();
                                     return childElement;
                                 }
                                 //比较名字
                                 if (childElement instanceof PsiIdentifier){
                                     PsiIdentifier  identifier= (PsiIdentifier) childElement;
                                     if (name.equals(identifier.getText())){
                                         find=true;
                                     }
                                 }

                             }
                         }
                     }
                 }
             }
             parent=parent.getParent();
         }

         return  null;
    }

    @Override
    public void afterCheckRootProject(@NotNull Context context) {
        super.afterCheckRootProject(context);
        if (filleUtils!=null){
            filleUtils.intput(hashMap);
        }
    }
}

