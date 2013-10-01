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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Recorder of method events for test verification.
 * 
 * @author Marc R. Hoffmann
 */
public class MethodRecorder {

    private final Printer printer;
    private final MethodVisitor visitor;

    public MethodRecorder() {
        printer = new Textifier();
        visitor = new TraceMethodVisitor(printer);
    }

    public Printer getPrinter() {
        return printer;
    }

    public MethodVisitor getVisitor() {
        return visitor;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodRecorder)) {
            return false;
        }
        MethodRecorder that = (MethodRecorder) obj;
        return printer.getText().equals(that.printer.getText());
    }

    @Override
    public int hashCode() {
        return printer.getText().hashCode();
    }

    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        printer.print(writer);
        writer.flush();
        return buffer.toString();
    }
}
