/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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
package org.ow2.asm.tree.analysis;

import org.ow2.asm.ClassReader;
import org.ow2.asm.ClassWriterComputeMaxsUnitTest;
import org.ow2.asm.MethodVisitor;
import org.ow2.asm.commons.EmptyVisitor;
import org.ow2.asm.tree.MethodNode;

/**
 * Analyzer unit tests for methods with JSR instructions.
 * 
 * @author Eric Bruneton
 */
public class AnalyzerUnitTest extends ClassWriterComputeMaxsUnitTest {

    @Override
    protected boolean isComputeMaxs() {
        return false;
    }

    @Override
    protected void assertMaxs(final int maxStack, final int maxLocals) {
        mv.visitMaxs(maxStack, maxLocals);
        mv.visitEnd();
        cw.visitEnd();
        byte[] b = cw.toByteArray();
        ClassReader cr = new ClassReader(b);
        cr.accept(new EmptyVisitor() {
            @Override
            public MethodVisitor visitMethod(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final String[] exceptions)
            {
                if (name.equals("m")) {
                    return new MethodNode(access,
                            name,
                            desc,
                            signature,
                            exceptions)
                    {
                        @Override
                        public void visitEnd() {
                            Analyzer<BasicValue> a = new Analyzer<BasicValue>(new BasicInterpreter());
                            try {
                                Frame<BasicValue>[] frames = a.analyze("C", this);
                                int mStack = 0;
                                int mLocals = 0;
                                for (int i = 0; i < frames.length; ++i) {
                                    if (frames[i] != null) {
                                        mStack = Math.max(mStack,
                                                frames[i].getStackSize());
                                        mLocals = Math.max(mLocals,
                                                frames[i].getLocals());
                                    }
                                }
                                assertEquals("maxStack", maxStack, mStack);
                                assertEquals("maxLocals", maxLocals, mLocals);
                            } catch (Exception e) {
                                fail(e.getMessage());
                            }
                        }
                    };
                } else {
                    return new EmptyVisitor();
                }
            }
        }, 0);

        try {
            TestClassLoader loader = new TestClassLoader();
            Class<?> c = loader.defineClass("C", b);
            c.newInstance();
        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }

    @Override
    protected void assertGraph(final String graph) {
    }
}