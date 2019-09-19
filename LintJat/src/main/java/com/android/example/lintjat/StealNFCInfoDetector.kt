package com.sollian.lintjar

import com.android.tools.lint.detector.api.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.MethodInsnNode

/**
 * @author zhoukewen
 * @since 2018/8/20
 */
class StealNFCInfoDetector : Detector(), ClassScanner {

    //LogUsage 是id是唯一的
    //参见 lint的xml文件
    val ISSUE = Issue.create(
            "NFCInfo",
            "私自调用NFC接口",
            "偷取NFC信息",
            Category.SECURITY, 5, Severity.ERROR,
            Implementation(StealNFCInfoDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )
    /**
     * 返回这个 Detector 适用的 ASM 指令
     */
    override fun getApplicableAsmNodeTypes(): IntArray? {
        //这里关心的是与方法调用相关的指令，其实就是以 INVOKE 开头的指令集
        return intArrayOf(AbstractInsnNode.METHOD_INSN)
    }
    /**
     * 扫描到 Detector 适用的指令时，回调此接口
     */

    override fun checkInstruction(context: ClassContext,
                                  classNode: org.objectweb.asm.tree.ClassNode,
                                  method: org.objectweb.asm.tree.MethodNode,
                                  instruction: org.objectweb.asm.tree.AbstractInsnNode
    ) {
        if (instruction.opcode != Opcodes.INVOKEVIRTUAL) {
            return
        }
        val callerMethodSig = classNode.name + "." + method.name + method.desc
        val methodInsn = instruction as MethodInsnNode
        // 这里逻辑是：调用 NfcAdapter 中的任何方法都会报告异常
        if (methodInsn.owner == "android/nfc/NfcAdapter") {
            val message = "SDK 中 $callerMethodSig 调用了 " +
                    "${methodInsn.owner.substringAfterLast('/')}.${methodInsn.name} 的方法来获取 NFC 信息，需要注意！"
            context.report(ISSUE, method, methodInsn, context.getLocation(instruction) , message)

        }
    }

}

