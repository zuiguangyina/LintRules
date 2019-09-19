package com.android.example.lintjat;

import com.intellij.psi.PsiElement;

import org.jetbrains.uast.UastUtils;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PsiUtils {

    //
    public static PsiElement getChildForDeep(PsiElement element, Class<? extends PsiElement> childType) {
        if (element == null || element.getChildren() == null)
            return null;
        //先序遍历，先比较自己
        if (element.getClass().isInstance(childType))
            return element;
        //在比较自己的孩子
        PsiElement[] children = element.getChildren();
        if (children.length != 0) {
            for (int pos = 0; pos < children.length; pos++) {
                PsiElement psiElement = children[pos];
                //递归遍历
                PsiElement reslut = getChildForDeep(psiElement, childType);
                if (reslut != null)
                    return reslut;
            }
        }
        return null;
    }

    public static PsiElement getChildForWidth(PsiElement element, Class<? extends PsiElement> childType) {
        if (element == null || element.getChildren() == null)
            return null;
        Queue<PsiElement> queue = new LinkedBlockingQueue();
        queue.add(element);
        while (!queue.isEmpty()) {
            PsiElement poll = queue.poll();
            if (poll.getClass().isInstance(childType))
                return poll;
            if (poll.getChildren() != null) {
                PsiElement[] children = poll.getChildren();
                int size = children.length;
                for (int pos = 0; pos < size; pos++) {
                    poll.add(children[pos]);
                }
            }
        }

        return null;
    }
}
