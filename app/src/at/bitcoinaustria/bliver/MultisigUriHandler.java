package at.bitcoinaustria.bliver;

import android.util.Log;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.WrongNetworkException;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author apetersson
 */
public class MultisigUriHandler {

    final Signer signer;
    private final HttpClient httpClient;

    public MultisigUriHandler(Signer signer) {
        this.signer = signer;
        httpClient = new DefaultHttpClient();
    }

    public void broadcastTransaction(MultisigUri uri) {
        String submitKey = uri.server_url + "/submit";
        uri.server_url.getHost(); uri.server_url.getScheme() ;
        requestPostKeyValue(URI.create(submitKey),"privkey",signer.getPrivateKey());
    }

    private final static String testurl = "multisig:server-url=http%3A%2F%2F10.200.1.73%2Fmultisig&order-id=123&order-description=testbestellung%20123";

    public String fromMultisigUri(MultisigUri uri) {
        // Create a new HttpClient and Post Header
        final String multisigAddr = requestPostKeyValue(uri.server_url, "pubkey", signer.getPublicKey());
        try {
            Address address = new Address(null, multisigAddr);
            String ret = BitcoinURI.convertToBitcoinURI(address, uri.amount.toBigInteger(), "order:" + uri.orderDesc, null);
            BitcoinURI testifOK = new BitcoinURI(ret);
            Preconditions.checkNotNull(testifOK);
            return ret;
        } catch (WrongNetworkException e) {
            throw new RuntimeException(e);
        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private String requestPostKeyValue(URI endpoint, String key, String value) {
        HttpPost httppost = new HttpPost(endpoint);

        final String multisigAddr;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair(key, value));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.i("BLIVER", "querying url:" + endpoint);
            HttpResponse response = httpClient.execute(httppost);
            HttpEntity responseEntity = Preconditions.checkNotNull(response.getEntity());
            String responseStr = EntityUtils.toString(responseEntity);
            ArrayList<String> lines = Lists.newArrayList(Splitter.on('\n').split(responseStr));
            multisigAddr = lines.get(0);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return multisigAddr;
    }
}
