/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.commons;

import static org.objectweb.asm.Opcodes.*;
import junit.framework.TestCase;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ClassInstrumenter}.
 * 
 * @author Marc R. Hoffmann
 */
public class FrameTrackingAdapterTest extends TestCase {

    static class FrameBuilder {

        private Object[] stack = new Object[0];
        private Object[] locals = new Object[0];

        FrameBuilder stack(Object... stack) {
            this.stack = stack;
            return this;
        }

        FrameBuilder locals(Object... locals) {
            this.locals = locals;
            return this;
        }

        void accept(MethodVisitor mv) {
            mv.visitFrame(F_NEW, locals.length, locals, stack.length, stack);
        }

    }

    private FrameBuilder before, after;

    private MethodNode mv;

    private Label label;

    @Override
    protected void setUp() {
        before = new FrameBuilder();
        after = new FrameBuilder();
        mv = new MethodNode(0, "test", "()V", null, null);
        label = new Label();
    }

    @Override
    protected void tearDown() {
        MethodRecorder actual = new MethodRecorder();
        MethodVisitor noLabels = new MethodVisitor(Opcodes.ASM4,
                actual.getVisitor()) {
            @Override
            public void visitLabel(Label label) {
                // Ignore labels inserted by the tracker
            }
        };
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", noLabels);
        before.accept(tracker);
        mv.instructions.accept(tracker);
        tracker.insertFrame();

        MethodRecorder expected = new MethodRecorder();
        before.accept(expected.getVisitor());
        mv.instructions.accept(expected.getVisitor());
        after.accept(expected.getVisitor());

        assertEquals(expected, actual);
    }

    public void testVisitFrameIllegalFrameType() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitFrame(F_APPEND, 0, null, 0, null);
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testVisitInsnIllegalOpcode() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitInsn(GOTO);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testVisitIntInsnIllegalOpcode() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitIntInsn(NOP, 0);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testVisitVarInsnIllegalOpcode() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitVarInsn(NOP, 0);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testVisitTypeInsnIllegalOpcode() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitTypeInsn(NOP, "A");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testVisitFieldInsnIllegalOpcode() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitFieldInsn(NOP, "A", "x", "I");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testVisitJumpInsnIllegalOpcode() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitJumpInsn(NOP, new Label());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testInvalidFrame1() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitInsn(POP);
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testInvalidFrame2() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitVarInsn(ALOAD, 1);
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testArgumentsConstructor() {
        FrameBuilder expectedFrame = new FrameBuilder();
        expectedFrame.locals(UNINITIALIZED_THIS);
        testArguments(0, "<init>", "()V", expectedFrame);
    }

    public void testArgumentsStatic() {
        FrameBuilder expectedFrame = new FrameBuilder();
        testArguments(Opcodes.ACC_STATIC, "test", "()V", expectedFrame);
    }

    public void testArgumentsStaticIJZ() {
        FrameBuilder expectedFrame = new FrameBuilder();
        expectedFrame.locals(INTEGER, LONG, INTEGER);
        testArguments(Opcodes.ACC_STATIC, "test", "(IJZ)V", expectedFrame);
    }

    public void testArgumentsStaticLArr() {
        FrameBuilder expectedFrame = new FrameBuilder();
        expectedFrame.locals("Foo", "[[S");
        testArguments(Opcodes.ACC_STATIC, "test", "(LFoo;[[S)V", expectedFrame);
    }

    public void testArgumentsFD() {
        FrameBuilder expectedFrame = new FrameBuilder();
        expectedFrame.locals("Test", FLOAT, DOUBLE);
        testArguments(0, "test", "(FD)V", expectedFrame);
    }

    private void testArguments(int access, String name, String desc,
            FrameBuilder expectedFrame) {
        MethodRecorder actual = new MethodRecorder();
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", access, name, desc, actual.getVisitor());
        tracker.insertFrame();

        MethodRecorder expected = new MethodRecorder();
        expectedFrame.accept(expected.getVisitor());

        assertEquals(expected, actual);
    }

    public void testFrameGaps() {
        before.locals().stack(INTEGER);
        mv.visitVarInsn(ISTORE, 3);
        after.locals(TOP, TOP, TOP, INTEGER).stack();
    }

    public void testLargeFrame() {
        before.locals("A", "B", "C", "D", "E").stack("AA", "BB", "CC", "DD",
                "EE");
        mv.visitInsn(NOP);
        after.locals("A", "B", "C", "D", "E").stack("AA", "BB", "CC", "DD",
                "EE");
    }

    public void testAALOAD_multidim_obj() {
        before.locals().stack("[[Ljava/lang/String;", INTEGER);
        mv.visitInsn(AALOAD);
        after.locals().stack("[Ljava/lang/String;");
    }

    public void testAALOAD_multidim_prim() {
        before.locals().stack("[[I", INTEGER);
        mv.visitInsn(AALOAD);
        after.locals().stack("[I");
    }

    public void testAASTORE() {
        before.locals().stack("[Ljava/lang/String;", INTEGER,
                "[Ljava/lang/String;");
        mv.visitInsn(AASTORE);
        after.locals().stack();
    }

    public void testACONST_NULL() {
        before.locals().stack();
        mv.visitInsn(ACONST_NULL);
        after.locals().stack(NULL);
    }

    public void testALOAD() {
        before.locals(LONG, "X", INTEGER).stack();
        mv.visitVarInsn(ALOAD, 2);
        after.locals(LONG, "X", INTEGER).stack("X");
    }

    public void testANEWARRAY() {
        before.locals().stack(INTEGER);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
        after.locals().stack("[Ljava/lang/String;");
    }

    public void testANEWARRAY_multidim_obj() {
        before.locals().stack(INTEGER);
        mv.visitTypeInsn(ANEWARRAY, "[Ljava/lang/String;");
        after.locals().stack("[[Ljava/lang/String;");
    }

    public void testANEWARRAY_multidim_prim() {
        before.locals().stack(INTEGER);
        mv.visitTypeInsn(ANEWARRAY, "[I");
        after.locals().stack("[[I");
    }

    public void testARETURN() {
        before.locals().stack("java/lang/Object");
        mv.visitInsn(ARETURN);
        after.locals().stack();
    }

    public void testARRAYLENGTH() {
        before.locals().stack("[Z");
        mv.visitInsn(ARRAYLENGTH);
        after.locals().stack(INTEGER);
    }

    public void testASTORE() {
        before.locals(LONG, "X", INTEGER).stack("Y");
        mv.visitVarInsn(ASTORE, 3);
        after.locals(LONG, "X", "Y").stack();
    }

    public void testATHROW() {
        before.locals().stack("java/lang/Exception");
        mv.visitInsn(ATHROW);
        after.locals().stack();
    }

    public void testBALOAD() {
        before.locals().stack("[B", INTEGER);
        mv.visitInsn(BALOAD);
        after.locals().stack(INTEGER);
    }

    public void testBASTORE() {
        before.locals().stack("[B", INTEGER, INTEGER);
        mv.visitInsn(BASTORE);
        after.locals().stack();
    }

    public void testBIPUSH() {
        before.locals().stack();
        mv.visitIntInsn(BIPUSH, 123);
        after.locals().stack(INTEGER);
    }

    public void testCALOAD() {
        before.locals().stack("[C", INTEGER);
        mv.visitInsn(CALOAD);
        after.locals().stack(INTEGER);
    }

    public void testCASTORE() {
        before.locals().stack("[C", INTEGER, INTEGER);
        mv.visitInsn(CASTORE);
        after.locals().stack();
    }

    public void testCHECKCAST() {
        before.locals().stack("java/lang/Object");
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        after.locals().stack("java/lang/String");
    }

    public void testD2F() {
        before.locals().stack(DOUBLE);
        mv.visitInsn(D2F);
        after.locals().stack(FLOAT);
    }

    public void testD2I() {
        before.locals().stack(DOUBLE);
        mv.visitInsn(D2I);
        after.locals().stack(INTEGER);
    }

    public void testD2L() {
        before.locals().stack(DOUBLE);
        mv.visitInsn(D2L);
        after.locals().stack(LONG);
    }

    public void testDADD() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DADD);
        after.locals().stack(DOUBLE);
    }

    public void testDALOAD() {
        before.locals().stack("[D", INTEGER);
        mv.visitInsn(DALOAD);
        after.locals().stack(DOUBLE);
    }

    public void testDASTORE() {
        before.locals().stack("[D", INTEGER, DOUBLE);
        mv.visitInsn(DASTORE);
        after.locals().stack();
    }

    public void testDCMPG() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DCMPG);
        after.locals().stack(INTEGER);
    }

    public void testDCMPL() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DCMPL);
        after.locals().stack(INTEGER);
    }

    public void testDCONST_0() {
        before.locals().stack();
        mv.visitInsn(DCONST_0);
        after.locals().stack(DOUBLE);
    }

    public void testDCONST_1() {
        before.locals().stack();
        mv.visitInsn(DCONST_1);
        after.locals().stack(DOUBLE);
    }

    public void testDDIV() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DDIV);
        after.locals().stack(DOUBLE);
    }

    public void testDLOAD() {
        before.locals(DOUBLE).stack();
        mv.visitVarInsn(DLOAD, 0);
        after.locals(DOUBLE).stack(DOUBLE);
    }

    public void testDMUL() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DMUL);
        after.locals().stack(DOUBLE);
    }

    public void testDNEG() {
        before.locals().stack(DOUBLE);
        mv.visitInsn(DNEG);
        after.locals().stack(DOUBLE);
    }

    public void testDREM() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DREM);
        after.locals().stack(DOUBLE);
    }

    public void testDRETURN() {
        before.locals().stack(DOUBLE);
        mv.visitInsn(DRETURN);
        after.locals().stack();
    }

    public void testDSTORE() {
        before.locals().stack(DOUBLE);
        mv.visitVarInsn(DSTORE, 0);
        after.locals(DOUBLE).stack();
    }

    public void testDSUB() {
        before.locals().stack(DOUBLE, DOUBLE);
        mv.visitInsn(DSUB);
        after.locals().stack(DOUBLE);
    }

    public void testDUP() {
        before.locals().stack("A");
        mv.visitInsn(DUP);
        after.locals().stack("A", "A");
    }

    public void testDUP2_one_two_word_item() {
        before.locals().stack(LONG);
        mv.visitInsn(DUP2);
        after.locals().stack(LONG, LONG);
    }

    public void testDUP2_two_one_word_items() {
        before.locals().stack("A", "B");
        mv.visitInsn(DUP2);
        after.locals().stack("A", "B", "A", "B");
    }

    public void testDUP_X1() {
        before.locals().stack("A", "B");
        mv.visitInsn(DUP_X1);
        after.locals().stack("B", "A", "B");
    }

    public void testDUP2_X1_one_two_word_item() {
        before.locals().stack("A", LONG);
        mv.visitInsn(DUP2_X1);
        after.locals().stack(LONG, "A", LONG);
    }

    public void testDUP2_X1_two_one_word_items() {
        before.locals().stack("A", "B", "C");
        mv.visitInsn(DUP2_X1);
        after.locals().stack("B", "C", "A", "B", "C");
    }

    public void testDUP_X2() {
        before.locals().stack("A", "B", "C");
        mv.visitInsn(DUP_X2);
        after.locals().stack("C", "A", "B", "C");
    }

    public void testDUP2_X2_one_two_word_item() {
        before.locals().stack("A", "B", LONG);
        mv.visitInsn(DUP2_X2);
        after.locals().stack(LONG, "A", "B", LONG);
    }

    public void testDUP2_X2_two_one_word_items() {
        before.locals().stack("A", "B", "C", "D");
        mv.visitInsn(DUP2_X2);
        after.locals().stack("C", "D", "A", "B", "C", "D");
    }

    public void testF2D() {
        before.locals().stack(FLOAT);
        mv.visitInsn(F2D);
        after.locals().stack(DOUBLE);
    }

    public void testF2I() {
        before.locals().stack(FLOAT);
        mv.visitInsn(F2I);
        after.locals().stack(INTEGER);
    }

    public void testF2L() {
        before.locals().stack(FLOAT);
        mv.visitInsn(F2L);
        after.locals().stack(LONG);
    }

    public void testFADD() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FADD);
        after.locals().stack(FLOAT);
    }

    public void testFALOAD() {
        before.locals().stack("[F", INTEGER);
        mv.visitInsn(FALOAD);
        after.locals().stack(FLOAT);
    }

    public void testFASTORE() {
        before.locals().stack("[F", INTEGER, FLOAT);
        mv.visitInsn(FASTORE);
        after.locals().stack();
    }

    public void testFCMPG() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FCMPG);
        after.locals().stack(INTEGER);
    }

    public void testFCMPL() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FCMPL);
        after.locals().stack(INTEGER);
    }

    public void testFCONST_0() {
        before.locals().stack();
        mv.visitInsn(FCONST_0);
        after.locals().stack(FLOAT);
    }

    public void testFCONST_1() {
        before.locals().stack();
        mv.visitInsn(FCONST_1);
        after.locals().stack(FLOAT);
    }

    public void testFCONST_2() {
        before.locals().stack();
        mv.visitInsn(FCONST_2);
        after.locals().stack(FLOAT);
    }

    public void testFDIV() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FDIV);
        after.locals().stack(FLOAT);
    }

    public void testFLOAD() {
        before.locals(FLOAT).stack();
        mv.visitVarInsn(FLOAD, 0);
        after.locals(FLOAT).stack(FLOAT);
    }

    public void testFMUL() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FMUL);
        after.locals().stack(FLOAT);
    }

    public void testFNEG() {
        before.locals().stack(FLOAT);
        mv.visitInsn(FNEG);
        after.locals().stack(FLOAT);
    }

    public void testFREM() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FREM);
        after.locals().stack(FLOAT);
    }

    public void testFRETURN() {
        before.locals().stack(FLOAT);
        mv.visitInsn(FRETURN);
        after.locals().stack();
    }

    public void testFSTORE() {
        before.locals().stack(FLOAT);
        mv.visitVarInsn(FSTORE, 0);
        after.locals(FLOAT).stack();
    }

    public void testFSUB() {
        before.locals().stack(FLOAT, FLOAT);
        mv.visitInsn(FSUB);
        after.locals().stack(FLOAT);
    }

    public void testGETFIELD() {
        before.locals().stack("Test");
        mv.visitFieldInsn(GETFIELD, "Test", "f", "I");
        after.locals().stack(INTEGER);
    }

    public void testGETSTATIC() {
        before.locals().stack();
        mv.visitFieldInsn(GETSTATIC, "Test", "f", "Z");
        after.locals().stack(INTEGER);
    }

    public void testGETSTATIC_float() {
        before.locals().stack();
        mv.visitFieldInsn(GETSTATIC, "Test", "f", "F");
        after.locals().stack(FLOAT);
    }

    public void testGETSTATIC_double() {
        before.locals().stack();
        mv.visitFieldInsn(GETSTATIC, "Test", "f", "D");
        after.locals().stack(DOUBLE);
    }

    public void testGOTO() {
        before.locals().stack();
        mv.visitJumpInsn(GOTO, label);
        after.locals().stack();
    }

    public void testI2B() {
        before.locals().stack(INTEGER);
        mv.visitInsn(I2B);
        after.locals().stack(INTEGER);
    }

    public void testI2C() {
        before.locals().stack(INTEGER);
        mv.visitInsn(I2C);
        after.locals().stack(INTEGER);
    }

    public void testI2D() {
        before.locals().stack(INTEGER);
        mv.visitInsn(I2D);
        after.locals().stack(DOUBLE);
    }

    public void testI2F() {
        before.locals().stack(INTEGER);
        mv.visitInsn(I2F);
        after.locals().stack(FLOAT);
    }

    public void testI2L() {
        before.locals().stack(INTEGER);
        mv.visitInsn(I2L);
        after.locals().stack(LONG);
    }

    public void testI2S() {
        before.locals().stack(INTEGER);
        mv.visitInsn(I2S);
        after.locals().stack(INTEGER);
    }

    public void testIADD() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IADD);
        after.locals().stack(INTEGER);
    }

    public void testIALOAD() {
        before.locals().stack("[I", INTEGER);
        mv.visitInsn(IALOAD);
        after.locals().stack(INTEGER);
    }

    public void testIAND() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IAND);
        after.locals().stack(INTEGER);
    }

    public void testIASTORE() {
        before.locals().stack("[I", INTEGER, INTEGER);
        mv.visitInsn(IASTORE);
        after.locals().stack();
    }

    public void testICONST_M1() {
        before.locals().stack();
        mv.visitInsn(ICONST_M1);
        after.locals().stack(INTEGER);
    }

    public void testICONST_0() {
        before.locals().stack();
        mv.visitInsn(ICONST_0);
        after.locals().stack(INTEGER);
    }

    public void testICONST_1() {
        before.locals().stack();
        mv.visitInsn(ICONST_1);
        after.locals().stack(INTEGER);
    }

    public void testICONST_2() {
        before.locals().stack();
        mv.visitInsn(ICONST_2);
        after.locals().stack(INTEGER);
    }

    public void testICONST_3() {
        before.locals().stack();
        mv.visitInsn(ICONST_3);
        after.locals().stack(INTEGER);
    }

    public void testICONST_4() {
        before.locals().stack();
        mv.visitInsn(ICONST_4);
        after.locals().stack(INTEGER);
    }

    public void testICONST_5() {
        before.locals().stack();
        mv.visitInsn(ICONST_5);
        after.locals().stack(INTEGER);
    }

    public void testIDIV() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IDIV);
        after.locals().stack(INTEGER);
    }

    public void testIF_ACMPEQ() {
        before.locals().stack("A", "A");
        mv.visitJumpInsn(IF_ACMPEQ, label);
        after.locals().stack();
    }

    public void testIF_ACMPNE() {
        before.locals().stack("A", "A");
        mv.visitJumpInsn(IF_ACMPNE, label);
        after.locals().stack();
    }

    public void testIF_ICMPEQ() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitJumpInsn(IF_ICMPEQ, label);
        after.locals().stack();
    }

    public void testIF_ICMPGE() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitJumpInsn(IF_ICMPGE, label);
        after.locals().stack();
    }

    public void testIF_ICMPGT() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitJumpInsn(IF_ICMPGT, label);
        after.locals().stack();
    }

    public void testIF_ICMPLE() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitJumpInsn(IF_ICMPLE, label);
        after.locals().stack();
    }

    public void testIF_ICMPLT() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitJumpInsn(IF_ICMPLT, label);
        after.locals().stack();
    }

    public void testIF_ICMPNE() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitJumpInsn(IF_ICMPNE, label);
        after.locals().stack();
    }

    public void testIFEQ() {
        before.locals().stack(INTEGER);
        mv.visitJumpInsn(IFEQ, label);
        after.locals().stack();
    }

    public void testIFGE() {
        before.locals().stack(INTEGER);
        mv.visitJumpInsn(IFGE, label);
        after.locals().stack();
    }

    public void testIFGT() {
        before.locals().stack(INTEGER);
        mv.visitJumpInsn(IFGT, label);
        after.locals().stack();
    }

    public void testIFLE() {
        before.locals().stack(INTEGER);
        mv.visitJumpInsn(IFLE, label);
        after.locals().stack();
    }

    public void testIFLT() {
        before.locals().stack(INTEGER);
        mv.visitJumpInsn(IFLT, label);
        after.locals().stack();
    }

    public void testIFNE() {
        before.locals().stack(INTEGER);
        mv.visitJumpInsn(IFNE, label);
        after.locals().stack();
    }

    public void testIFNONNULL() {
        before.locals().stack("A");
        mv.visitJumpInsn(IFNONNULL, label);
        after.locals().stack();
    }

    public void testIFNULL() {
        before.locals().stack("A");
        mv.visitJumpInsn(IFNULL, label);
        after.locals().stack();
    }

    public void testIINC() {
        before.locals(INTEGER).stack();
        mv.visitIincInsn(0, 1);
        after.locals(INTEGER).stack();
    }

    public void testILOAD() {
        before.locals(INTEGER).stack();
        mv.visitVarInsn(ILOAD, 0);
        after.locals(INTEGER).stack(INTEGER);
    }

    public void testIMUL() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IMUL);
        after.locals().stack(INTEGER);
    }

    public void testINEG() {
        before.locals().stack(INTEGER);
        mv.visitInsn(INEG);
        after.locals().stack(INTEGER);
    }

    public void testINSTANCEOF() {
        before.locals().stack("java/lang/String");
        mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
        after.locals().stack(INTEGER);
    }

    public void testINVOKEDYNAMIC() {
        before.locals().stack("java/lang/String");
        mv.visitInvokeDynamicInsn("foo", "(Ljava/lang/String;)I", new Handle(0,
                null, null, null));
        after.locals().stack(INTEGER);
    }

    public void testINVOKEINTERFACE() {
        before.locals().stack("Test");
        mv.visitMethodInsn(INVOKEVIRTUAL, "Test", "getSize", "()I");
        after.locals().stack(INTEGER);
    }

    public void testINVOKESPECIAL() {
        before.locals().stack("Test", LONG, LONG);
        mv.visitMethodInsn(INVOKEVIRTUAL, "Test", "add", "(JJ)J");
        after.locals().stack(LONG);
    }

    public void testINVOKESPECIAL_initsuper() {
        before.locals(UNINITIALIZED_THIS).stack(UNINITIALIZED_THIS);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "<init>", "()V");
        after.locals("Test").stack();
    }

    public void testINVOKESTATIC() {
        before.locals().stack(LONG, LONG);
        mv.visitMethodInsn(INVOKESTATIC, "Test", "add", "(JJ)J");
        after.locals().stack(LONG);
    }

    public void testINVOKEVIRTUAL() {
        before.locals().stack("Test", INTEGER, DOUBLE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "Test", "run", "(ID)V");
        after.locals().stack();
    }

    public void testIOR() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IOR);
        after.locals().stack(INTEGER);
    }

    public void testIREM() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IREM);
        after.locals().stack(INTEGER);
    }

    public void testIRETURN() {
        before.locals().stack(INTEGER);
        mv.visitInsn(IRETURN);
        after.locals().stack();
    }

    public void testISHL() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(ISHL);
        after.locals().stack(INTEGER);
    }

    public void testISHR() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(ISHR);
        after.locals().stack(INTEGER);
    }

    public void testISSTORE() {
        before.locals().stack(INTEGER);
        mv.visitVarInsn(ISTORE, 0);
        after.locals(INTEGER).stack();
    }

    public void testISUB() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(ISUB);
        after.locals().stack(INTEGER);
    }

    public void testIUSHR() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IUSHR);
        after.locals().stack(INTEGER);
    }

    public void testIXOR() {
        before.locals().stack(INTEGER, INTEGER);
        mv.visitInsn(IXOR);
        after.locals().stack(INTEGER);
    }

    public void testL2D() {
        before.locals().stack(LONG);
        mv.visitInsn(L2D);
        after.locals().stack(DOUBLE);
    }

    public void testL2F() {
        before.locals().stack(LONG);
        mv.visitInsn(L2F);
        after.locals().stack(FLOAT);
    }

    public void testL2I() {
        before.locals().stack(LONG);
        mv.visitInsn(L2I);
        after.locals().stack(INTEGER);
    }

    public void testLADD() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LADD);
        after.locals().stack(LONG);
    }

    public void testLALOAD() {
        before.locals().stack("L[", INTEGER);
        mv.visitInsn(LALOAD);
        after.locals().stack(LONG);
    }

    public void testLAND() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LAND);
        after.locals().stack(LONG);
    }

    public void testLASTORE() {
        before.locals().stack("L[", INTEGER, LONG);
        mv.visitInsn(LASTORE);
        after.locals().stack();
    }

    public void testLCMP() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LCMP);
        after.locals().stack(INTEGER);
    }

    public void testLCONST_0() {
        before.locals().stack();
        mv.visitInsn(LCONST_0);
        after.locals().stack(LONG);
    }

    public void testLCONST_1() {
        before.locals().stack();
        mv.visitInsn(LCONST_1);
        after.locals().stack(LONG);
    }

    public void testLDC_int() {
        before.locals().stack();
        mv.visitLdcInsn(Integer.valueOf(123));
        after.locals().stack(INTEGER);
    }

    public void testLDC_float() {
        before.locals().stack();
        mv.visitLdcInsn(Float.valueOf(123));
        after.locals().stack(FLOAT);
    }

    public void testLDC_long() {
        before.locals().stack();
        mv.visitLdcInsn(Long.valueOf(123));
        after.locals().stack(LONG);
    }

    public void testLDC_double() {
        before.locals().stack();
        mv.visitLdcInsn(Double.valueOf(123));
        after.locals().stack(DOUBLE);
    }

    public void testLDC_String() {
        before.locals().stack();
        mv.visitLdcInsn("Hello VM!");
        after.locals().stack("java/lang/String");
    }

    public void testLDC_Class() {
        before.locals().stack();
        mv.visitLdcInsn(Type.getType("[java/lang/Runnable;"));
        after.locals().stack("java/lang/Class");
    }

    public void testLDC_invalidType() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", null);
        try {
            tracker.visitLdcInsn(Byte.valueOf((byte) 123));
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testLDIV() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LDIV);
        after.locals().stack(LONG);
    }

    public void testLLOAD() {
        before.locals(LONG).stack();
        mv.visitVarInsn(LLOAD, 0);
        after.locals(LONG).stack(LONG);
    }

    public void testLMUL() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LMUL);
        after.locals().stack(LONG);
    }

    public void testLNEG() {
        before.locals().stack(LONG);
        mv.visitInsn(LNEG);
        after.locals().stack(LONG);
    }

    public void testLOOKUPSWITCH() {
        before.locals().stack(INTEGER);
        mv.visitLookupSwitchInsn(new Label(), new int[0], new Label[0]);
        after.locals().stack();
    }

    public void testLOR() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LOR);
        after.locals().stack(LONG);
    }

    public void testLREM() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LREM);
        after.locals().stack(LONG);
    }

    public void testLRETURN() {
        before.locals().stack(LONG);
        mv.visitInsn(LRETURN);
        after.locals().stack();
    }

    public void testLSHL() {
        before.locals().stack(LONG, INTEGER);
        mv.visitInsn(LSHL);
        after.locals().stack(LONG);
    }

    public void testLSHR() {
        before.locals().stack(LONG, INTEGER);
        mv.visitInsn(LSHR);
        after.locals().stack(LONG);
    }

    public void testLSTORE() {
        before.locals().stack(LONG);
        mv.visitVarInsn(LSTORE, 0);
        after.locals(LONG).stack();
    }

    public void testLSUB() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LSUB);
        after.locals().stack(LONG);
    }

    public void testLUSHR() {
        before.locals().stack(LONG, INTEGER);
        mv.visitInsn(LUSHR);
        after.locals().stack(LONG);
    }

    public void testLXOR() {
        before.locals().stack(LONG, LONG);
        mv.visitInsn(LXOR);
        after.locals().stack(LONG);
    }

    public void testMONITORENTER() {
        before.locals().stack("java/lang/Object");
        mv.visitInsn(MONITORENTER);
        after.locals().stack();
    }

    public void testMONITOREXIT() {
        before.locals().stack("java/lang/Object");
        mv.visitInsn(MONITOREXIT);
        after.locals().stack();
    }

    public void testMULTIANEWARRAY() {
        before.locals().stack(INTEGER, INTEGER, INTEGER);
        mv.visitMultiANewArrayInsn("[[[Ljava/lang/String;", 3);
        after.locals().stack("[[[Ljava/lang/String;");
    }

    public void testNEW() {
        before.locals(LONG).stack(LONG);
        mv.visitTypeInsn(NEW, "Test");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "Test", "<init>", "()V");
        after.locals(LONG).stack(LONG, "Test");
    }

    public void testNEWARRAY_boolean() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        after.locals().stack("[Z");
    }

    public void testNEWARRAY_char() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_CHAR);
        after.locals().stack("[C");
    }

    public void testNEWARRAY_float() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_FLOAT);
        after.locals().stack("[F");
    }

    public void testNEWARRAY_double() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        after.locals().stack("[D");
    }

    public void testNEWARRAY_byte() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        after.locals().stack("[B");
    }

    public void testNEWARRAY_short() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_SHORT);
        after.locals().stack("[S");
    }

    public void testNEWARRAY_int() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_INT);
        after.locals().stack("[I");
    }

    public void testNEWARRAY_long() {
        before.locals().stack(INTEGER);
        mv.visitIntInsn(NEWARRAY, T_LONG);
        after.locals().stack("[J");
    }

    public void testNEWARRAY_invalidOperand() {
        FrameTrackingAdapter tracker = new FrameTrackingAdapter(Opcodes.ASM4,
                "Test", ACC_STATIC, "test", "()V", new MethodNode());
        tracker.visitFrame(F_NEW, 0, new Object[0], 1, new Object[] { INTEGER });
        try {
            tracker.visitIntInsn(NEWARRAY, -1);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testNOP() {
        before.locals().stack();
        mv.visitInsn(NOP);
        after.locals().stack();
    }

    public void testPOP() {
        before.locals().stack(INTEGER);
        mv.visitInsn(POP);
        after.locals().stack();
    }

    public void testPOP2_one_two_word_item() {
        before.locals().stack(DOUBLE);
        mv.visitInsn(POP2);
        after.locals().stack();
    }

    public void testPOP2_two_one_word_items() {
        before.locals().stack("A", INTEGER);
        mv.visitInsn(POP2);
        after.locals().stack();
    }

    public void testPUTFIELD() {
        before.locals().stack("Test", INTEGER);
        mv.visitFieldInsn(PUTFIELD, "Test", "field", "I");
        after.locals().stack();
    }

    public void testPUTSTATIC() {
        before.locals().stack(INTEGER);
        mv.visitFieldInsn(PUTSTATIC, "Test", "field", "I");
        after.locals().stack();
    }

    public void testRETURN() {
        before.locals().stack();
        mv.visitInsn(RETURN);
        after.locals().stack();
    }

    public void testSALOAD() {
        before.locals().stack("[S", INTEGER);
        mv.visitInsn(SALOAD);
        after.locals().stack(INTEGER);
    }

    public void testSASTORE() {
        before.locals().stack("[S", INTEGER, INTEGER);
        mv.visitInsn(SASTORE);
        after.locals().stack();
    }

    public void testSIPUSH() {
        before.locals().stack();
        mv.visitIntInsn(SIPUSH, 123);
        after.locals().stack(INTEGER);
    }

    public void testSWAP() {
        before.locals().stack("A", "B");
        mv.visitInsn(SWAP);
        after.locals().stack("B", "A");
    }

    public void testTABLESWITCH() {
        before.locals().stack(INTEGER);
        mv.visitTableSwitchInsn(0, 1, new Label(), new Label[0]);
        after.locals().stack();
    }

}
