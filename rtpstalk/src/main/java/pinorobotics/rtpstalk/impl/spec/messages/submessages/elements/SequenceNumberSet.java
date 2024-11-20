/*
 * Copyright 2022 pinorobotics
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
package pinorobotics.rtpstalk.impl.spec.messages.submessages.elements;

import id.xfunction.Preconditions;
import id.xfunction.XJsonStringBuilder;
import id.xfunction.util.IntBitSet;
import java.util.List;
import java.util.stream.LongStream;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedInt;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;

/**
 * SequenceNumberSet SubmessageElements are used as parts of several messages to provide binary
 * information about individual sequence numbers within a range. The sequence numbers represented in
 * the SequenceNumberSet are limited to belong to an interval with a range no bigger than 256.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SequenceNumberSet implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS = List.of("bitmapBase", "numBits", "bitmap");

    /** Bitmap of up to 256 bits */
    public static final int BITMAP_SIZE = 256;

    public static final int BITMAP_SIZE_IN_INTS = BITMAP_SIZE / Integer.SIZE;

    @RtpsSpecReference(
            paragraph = "9.4.2.6",
            protocolVersion = Predefined.Version_2_3,
            text = "First sequence number in the set, must be >= 1")
    public SequenceNumber bitmapBase = new SequenceNumber(1);

    /** Number of bits in a set */
    public UnsignedInt numBits;

    public int[] bitmap = new int[0];

    public SequenceNumberSet() {}

    public SequenceNumberSet(long bitmapBase, int numBits, int... bitmap) {
        this(new SequenceNumber(bitmapBase), numBits, bitmap);
    }

    public SequenceNumberSet(SequenceNumber bitmapBase, int numBits, int... bitmap) {
        this.bitmapBase = bitmapBase;
        this.numBits = new UnsignedInt(numBits);
        if (bitmap.length == 0) return;
        this.bitmap = bitmap;
        validate();
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("bitmapBase", bitmapBase);
        builder.append("numBits", numBits);
        builder.append("bitmap", bitmap);
        return builder.toString();
    }

    @RtpsSpecReference(
            paragraph = "8.3.5.5",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The sequence numbers represented in the SequenceNumberSet are limited to\n"
                            + "belong to an interval with a range no bigger than 256")
    public void validate() {
        Preconditions.isLess(0, bitmapBase.value, "Start number is too small");
        Preconditions.isTrue(
                bitmap.length <= BITMAP_SIZE_IN_INTS,
                "Bitmap size should not exceed " + BITMAP_SIZE_IN_INTS);
    }

    public LongStream stream() {
        return new IntBitSet(bitmap).streamOfSetBits().mapToLong(i -> bitmapBase.value + i);
    }
}
