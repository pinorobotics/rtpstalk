package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XAsserts;
import id.xfunction.XJsonStringBuilder;

/**
 * SequenceNumberSet SubmessageElements are used as parts of several messages to
 * provide binary information about individual sequence numbers within a range.
 * The sequence numbers represented in the SequenceNumberSet are limited to
 * belong to an interval with a range no bigger than 256.
 *
 */
public class SequenceNumberSet {

    /**
     * First sequence number in the set, must be >= 1 (9.4.2.6 SequenceNumberSet)
     */
    public SequenceNumber bitmapBase = new SequenceNumber(1);

    /** Number of bits in a set */
    public int numBits;

    /** Bitmap of up to 256 bits */
    public int[] bitmap = new int[0];

    public SequenceNumberSet() {

    }

    public SequenceNumberSet(long bitmapBase, int numBits, int... bitmap) {
        this(new SequenceNumber(bitmapBase), numBits, bitmap);
    }

    public SequenceNumberSet(SequenceNumber bitmapBase, int numBits, int... bitmap) {
        this.bitmapBase = bitmapBase;
        this.numBits = numBits;
        if (bitmap.length == 0)
            return;
        XAsserts.assertTrue(bitmap.length <= 8, "Bitmap size should not exceed 8");
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
