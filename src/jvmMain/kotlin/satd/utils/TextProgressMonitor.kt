/*
 * Copyright (C) 2007, Robin Rosenberg <robin.rosenberg@dewire.com>
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package satd.utils

import org.eclipse.jgit.lib.BatchingProgressMonitor

/**
 * A simple progress reporter printing on a stream.
 */
class TextProgressMonitor(val prepend: String) : BatchingProgressMonitor() {

    /**
     * {@inheritDoc}
     */
    override fun onUpdate(taskName: String, workCurr: Int) {
        val s = StringBuilder()
        format(s, taskName, workCurr)
        send(s)
    }

    /**
     * {@inheritDoc}
     */
    override fun onEndTask(taskName: String, workCurr: Int) {
        val s = StringBuilder()
        format(s, taskName, workCurr)
        //s.append("\n") //$NON-NLS-1$
        send(s)
    }

    private fun format(s: StringBuilder, taskName: String, workCurr: Int) {
        s.append("\r") //$NON-NLS-1$
        s.append(taskName)
        s.append(": ") //$NON-NLS-1$
        while (s.length < 25)
            s.append(' ')
        s.append(workCurr)
    }

    /**
     * {@inheritDoc}
     */
    override fun onUpdate(taskName: String, cmp: Int, totalWork: Int, pcnt: Int) {
        val s = StringBuilder()
        format(s, taskName, cmp, totalWork, pcnt)
        send(s)
    }

    /**
     * {@inheritDoc}
     */
    override fun onEndTask(taskName: String, cmp: Int, totalWork: Int, pcnt: Int) {
        val s = StringBuilder()
        format(s, taskName, cmp, totalWork, pcnt)
        //s.append("\n") //$NON-NLS-1$
        send(s)
    }

    private fun format(
        s: StringBuilder, taskName: String, cmp: Int,
        totalWork: Int, pcnt: Int
    ) {
        //s.append("\r") //$NON-NLS-1$
        s.append(taskName)
        s.append(": ") //$NON-NLS-1$
        while (s.length < 25)
            s.append(' ')

        val endStr = totalWork.toString()
        var curStr = cmp.toString()
        while (curStr.length < endStr.length)
            curStr = " $curStr" //$NON-NLS-1$
        if (pcnt < 100)
            s.append(' ')
        if (pcnt < 10)
            s.append(' ')
        s.append(pcnt)
        s.append("% (") //$NON-NLS-1$
        s.append(curStr)
        s.append("/") //$NON-NLS-1$
        s.append(endStr)
        s.append(")") //$NON-NLS-1$
    }

    private fun send(s: StringBuilder) {
        logln(prepend.padEnd(55) + ' ' + s.toString())
    }
}
