/*
 * Copyright 2024 pinorobotics
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
package pinorobotics.rtpstalk.tests.integration.thirdparty;

import id.pubsubtests.data.Message;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
class StringMessage extends Message {

    public StringMessage(byte[] body) {
        super(body);
        convertToSingleString(getBody());
    }

    static void convertToSingleString(byte[] buf) {
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == 0 || buf[i] == '\n') buf[i] = ' ';
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringMessage o) {
            // performs null terminated string comparison
            var b = o.getBody();
            for (int i = 0; i < b.length; i++) {
                if (b[i] != getBody()[i]) return false;
                if (b[i] == 0) return true;
            }
            return true;
        }
        return false;
    }
}
