package at.bitcoinaustria.bliver;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.bitcoin.core.PeerGroup;

public class PackageListActivity extends FragmentActivity
        implements PackageListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_package_list);

        if (findViewById(R.id.package_detail_container) != null) {
            mTwoPane = true;
            ((PackageListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.package_list))
                    .setActivateOnItemClick(true);
        }

        final Intent intent = getIntent();
        final Uri data = intent.getData();
        if (data != null) {
            new AsyncTask<Void,Void,String>(){
                @Override
                protected String doInBackground(Void... params) {
                    final MultisigUri multisigUri = MultisigUri.from(data.toString());
                    return new MultisigUriHandler(Signer.DEMO_SIGNER).fromMultisigUri(multisigUri);
                }

                @Override
                protected void onPostExecute(String bitcoinURI) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(bitcoinURI));
                    startActivity(i);
                }
            }.execute();

            int debug = 0;
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PackageDetailFragment.ARG_ITEM_ID, id);
            PackageDetailFragment fragment = new PackageDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.package_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, PackageDetailActivity.class);
            detailIntent.putExtra(PackageDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    private static void unusedBla() {
        new PeerGroup(null, null);
    }
}
