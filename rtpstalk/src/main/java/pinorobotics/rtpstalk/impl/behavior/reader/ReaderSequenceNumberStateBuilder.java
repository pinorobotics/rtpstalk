/*
 * Copyright 2023 pinorobotics
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
package pinorobotics.rtpstalk.impl.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.util.IntBitSet;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;

/**
 * Build {@link SequenceNumberSet} of sequence numbers which changes are missing on the Reader
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReaderSequenceNumberStateBuilder {

    public SequenceNumberSet build(
            long firstMissing, long lastMissing, long[] missingSorted, long availableChangesMax) {
        if (missingSorted.length == 0) {
            return expectNextSet(availableChangesMax);
        }
        Preconditions.isTrue(firstMissing <= lastMissing, "Sorted array of changes expected");
        firstMissing = Math.max(firstMissing, missingSorted[0]);
        lastMissing = Math.min(lastMissing, missingSorted[missingSorted.length - 1]);
        // enumeration from 1
        var numBits = (int) (lastMissing - firstMissing + 1);
        numBits = Math.min(SequenceNumberSet.BITMAP_SIZE, numBits);

        // Creates bitmask of missing changes between [firstMissing..lastMissing]
        var bset = new IntBitSet(numBits);
        for (var sn : missingSorted) {
            if (sn >= (firstMissing + numBits)) continue;
            if (sn < firstMissing || lastMissing < sn) continue;
            bset.flip((int) (sn - firstMissing));
        }

        return new SequenceNumberSet(firstMissing, numBits, bset.intArray());
    }

    private SequenceNumberSet expectNextSet(long availableChangesMax) {
        // all present so we expect next
        return new SequenceNumberSet(new SequenceNumber(availableChangesMax + 1), 0);
    }
}
