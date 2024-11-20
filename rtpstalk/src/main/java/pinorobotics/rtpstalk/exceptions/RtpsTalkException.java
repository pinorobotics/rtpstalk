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
package pinorobotics.rtpstalk.exceptions;

/**
 * Generic runtime exception for all <b>rtpstalk</b> operations.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RtpsTalkException() {
        super();
    }

    public RtpsTalkException(String message) {
        super(message);
    }

    public RtpsTalkException(String fmt, Object... objs) {
        super(String.format(fmt, objs));
    }

    public RtpsTalkException(Exception e) {
        super(e);
    }
}
