/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.Preconditions;
import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.spec.RtpsSpecReference;

/**
 * SequenceNumberSet SubmessageElements are used as parts of several messages to provide binary
 * information about individual sequence numbers within a range. The sequence numbers represented in
 * the SequenceNumberSet are limited to belong to an interval with a range no bigger than 256.
 */
/** @author aeon_flux aeon_flux@eclipso.ch */
public class SequenceNumberSet {

    @RtpsSpecReference(
            paragraph = "9.4.2.6",
            protocolVersion = Predefined.Version_2_3,
            text = "First sequence number in the set, must be >= 1")
    public SequenceNumber bitmapBase = new SequenceNumber(1);

    /** Number of bits in a set */
    public int numBits;

    /** Bitmap of up to 256 bits */
    public int[] bitmap = new int[0];

    public SequenceNumberSet() {}

    public SequenceNumberSet(long bitmapBase, int numBits, int... bitmap) {
        this(new SequenceNumber(bitmapBase), numBits, bitmap);
    }

    public SequenceNumberSet(SequenceNumber bitmapBase, int numBits, int... bitmap) {
        this.bitmapBase = bitmapBase;
        this.numBits = numBits;
        if (bitmap.length == 0) return;
        Preconditions.isTrue(bitmap.length <= 8, "Bitmap size should not exceed 8");
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("bitmapBase", bitmapBase);
        builder.append("bitmap", bitmap);
        return builder.toString();
    }
}
