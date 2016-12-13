/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package spacegraph.net.vnc.rfb.codec.handshaker;

import spacegraph.net.vnc.rfb.codec.ProtocolVersion;

class RfbClient38Handshaker extends RfbClientHandshaker {

    public RfbClient38Handshaker(ProtocolVersion version) {
        super(version);
    }

    @Override
    public RfbClientDecoder newRfbClientDecoder() {
        return new RfbClient38Decoder();
    }

    @Override
    public RfbClientEncoder newRfbClientEncoder() {
        return new RfbClient38Encoder();
    }

}