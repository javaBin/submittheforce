package no.java.submit.util;

import com.google.common.io.BaseEncoding;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

public class HmacHelper {

    private HmacUtils hmacUtils;

    public HmacHelper(String secret) {
        this.hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
    }

    public String create(String source) {
        return BaseEncoding.base32().omitPadding().encode(hmacUtils.hmac(source));
    }
}
