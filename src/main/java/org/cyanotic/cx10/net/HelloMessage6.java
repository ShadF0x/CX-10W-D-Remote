package org.cyanotic.cx10.net;

import static org.cyanotic.cx10.utils.ByteUtils.asUnsigned;

/**
 * Created by orfeo.ciano on 18/11/2016.
 */
public class HelloMessage6 extends HelloMessage1 {

    public HelloMessage6() {
        super();
        token = asUnsigned(0xb5, 0x4f, 0x38, 0xc7, 0x5c, 0xe4, 0x9a, 0xca, 0xa9, 0x0a, 0x73, 0x7d, 0x7d, 0x84, 0xdb, 0xdc);
        line1 = asUnsigned(0xac, 0xef, 0x63, 0xf7, 0x71, 0x57, 0xab, 0x2f, 0x53, 0xe3, 0xf7, 0x68, 0xec, 0xd9, 0xe1, 0x85);
        line2 = asUnsigned(0x47, 0xb8, 0xc2, 0x2e, 0x21, 0xd0, 0x1b, 0xfb, 0x6b, 0x3d, 0xe3, 0x25, 0xa2, 0x7b, 0x8f, 0xb3);
        line3 = asUnsigned(0xac, 0xef, 0x63, 0xf7, 0x71, 0x57, 0xab, 0x2f, 0x53, 0xe3, 0xf7, 0x68, 0xec, 0xd9, 0xe1, 0x85);
        line4 = asUnsigned(0xee, 0x2e, 0x09, 0xa3, 0x9b, 0xdd, 0x05, 0xc8, 0x30, 0xa2, 0x81, 0xc8, 0x2a, 0x9e, 0xda, 0x7f);
        line5 = asUnsigned(0x74, 0xea, 0xfe, 0x12, 0x4e, 0x23, 0x9d, 0x74, 0x76, 0x16, 0x10, 0x6c, 0x5a, 0x9c, 0xd8, 0xb7);
    }

    @Override
    byte getTypeFlag() {
        return 0x5d;
    }

    @Override
    public String toString() {
        return "Hello6\n" + super.toString();
    }
}
