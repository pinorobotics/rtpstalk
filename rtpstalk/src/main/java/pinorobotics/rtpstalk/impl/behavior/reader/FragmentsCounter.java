/*
 * Copyright 2023 rtpstalk project
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

import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;

/**
 * Fragments enumeration starts from 1 (see {@link DataFrag#fragmentStartingNum})
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class FragmentsCounter {
    private boolean[] isFragmentReceived;
    private int missingFragmentsCount;

    public FragmentsCounter(int numOfFragments) {
        isFragmentReceived = new boolean[numOfFragments];
        missingFragmentsCount = numOfFragments;
    }

    public void markAllFragmentsAsPresent(int fragmentStartingNum, int fragmentEndingNum) {
        for (int fragmentNum = fragmentStartingNum;
                fragmentNum < fragmentEndingNum;
                fragmentNum++) {
            var i = fragmentNum - 1;
            if (isFragmentReceived[i]) continue;
            isFragmentReceived[i] = true;
            missingFragmentsCount--;
        }
    }

    public boolean isAnyFragmentPresent(int fragmentStartingNum, int fragmentEndingNum) {
        for (int fragmentNum = fragmentStartingNum;
                fragmentNum < fragmentEndingNum;
                fragmentNum++) {
            if (isFragmentReceived[fragmentNum - 1]) return true;
        }
        return false;
    }

    public int getMissingFragmentsCount() {
        return missingFragmentsCount;
    }
}
