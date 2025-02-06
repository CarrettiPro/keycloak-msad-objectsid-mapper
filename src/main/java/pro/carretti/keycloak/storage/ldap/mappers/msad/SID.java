package pro.carretti.keycloak.storage.ldap.mappers.msad;

import java.io.IOException;
import org.keycloak.common.util.Base64;

public class SID {

    private int revisionLevel;
    private int subAuthorityCount;
    private int authority;
    private long[] subAuthorities;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("S-%d-%d", this.revisionLevel, this.authority));
	for (long v : this.subAuthorities) {
            sb.append(String.format("-%d", v));
	}
        return sb.toString();
    }

    public static SID decode(String s) throws IOException {
        return decode(Base64.decode(s));
    }

    public static SID decode(byte[] b) {
	SID sid = new SID();

        // get byte(0) - revision level
        sid.revisionLevel = b[0];

        //next byte byte(1) - count of sub-authorities
        sid.subAuthorityCount = b[1] & 0xFF;
        sid.subAuthorities = new long[sid.subAuthorityCount];

        //byte(2-7) - 48 bit authority ([Big-Endian])
        for (int i = 2; i <= 7; i++) {
            sid.authority |= ((long) b[i]) << (8 * (5 - (i - 2)));
        }

        //iterate all the sub-auths and then countSubAuths x 32 bit sub authorities ([Little-Endian])
        int offset = 8;
        int size = 4; //4 bytes for each sub auth
        for (int j = 0; j < sid.subAuthorityCount; j++) {
            for (int k = 0; k < size; k++) {
                sid.subAuthorities[j] |= (long) (b[offset + k] & 0xFF) << (8 * k);
            }
            offset += size;
        }

	return sid;
    }

}
