package com.my.awesome.app;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// new added
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;

import com.my.awesome.app.data.main.DemoMenuItem;
import com.my.awesome.app.data.main.FooterType;
import com.my.awesome.app.data.main.ListItem;
import com.my.awesome.app.data.main.SectionHeader;
import com.my.awesome.app.ui.MainRecyclerViewAdapter;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The main {@link android.app.Activity} of this app.
 * <p>
 * Created by santoshbagadi on 2019-09-10.
 */
public class MainActivity
        extends AppCompatActivity
        implements MainRecyclerViewAdapter.OnMainListItemClickListener, MaxAdListener
{
    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        final MainRecyclerViewAdapter adapter = new MainRecyclerViewAdapter( generateMainListItems(), this, this );
        final LinearLayoutManager manager = new LinearLayoutManager( this );
        final DividerItemDecoration decoration = new DividerItemDecoration( this, manager.getOrientation() );

        final RecyclerView recyclerView = findViewById( R.id.main_recycler_view );
        recyclerView.setHasFixedSize( true );
        recyclerView.setLayoutManager( manager );
        recyclerView.addItemDecoration( decoration );
        recyclerView.setItemAnimator( new DefaultItemAnimator() );
        recyclerView.setAdapter( adapter );

        AppLovinSdk.getInstance( this ).setMediationProvider( AppLovinMediationProvider.MAX );
        AppLovinSdk.initializeSdk(this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final  AppLovinSdkConfiguration configuration) {
                // AppLovin SDK is initialized, start loading ads
            }
        });

        // Create interstitial ad and load it
        createInterstitialAd();
    }

    void createInterstitialAd()
    {
        interstitialAd = new MaxInterstitialAd( "inter_ad_unitID", this );
        interstitialAd.setListener( this );

        // Load the first ad
        interstitialAd.loadAd();
    }

    private List<ListItem> generateMainListItems()
    {
        final List<ListItem> items = new ArrayList<>();
        items.add( new SectionHeader( "APPLOVIN" ) );
        items.add( new DemoMenuItem( "Show Interstitial", () -> {
            if(interstitialAd.isReady()) {
                interstitialAd.showAd();
            } else {
                Log.d("AppLovin", "Inter ad is not ready!");
            }
        } ) );
        items.add( new DemoMenuItem( "Launch Mediation Debugger",
                () -> AppLovinSdk.getInstance( getApplicationContext() ).showMediationDebugger() ) );
        items.add( new FooterType() );
        return items;
    }

    @Override
    public void onItemClicked(final ListItem item)
    {
        if ( item.getType() == ListItem.TYPE_AD_ITEM )
        {
            final DemoMenuItem demoMenuItem = (DemoMenuItem) item;
            if ( demoMenuItem.getIntent() != null )
            {
                startActivity( demoMenuItem.getIntent() );
            }
            else if ( demoMenuItem.getRunnable() != null )
            {
                demoMenuItem.getRunnable().run();
            }
        }
    }

    // MAX Ad Listener
    @Override
    public void onAdLoaded(final MaxAd maxAd)
    {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

        // Reset retry attempt
        retryAttempt = 0;
    }

    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error)
    {
        // Interstitial ad failed to load
        System.out.println("Inter Ad load failed: " + error);
        // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

        new Handler().postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                interstitialAd.loadAd();
            }
        }, delayMillis );
    }

    @Override
    public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
    {
        // Interstitial ad failed to display. We recommend loading the next ad
        interstitialAd.loadAd();
    }

    @Override
    public void onAdDisplayed(final MaxAd maxAd) {
        Log.d("Ad Listener", "Inter ad is displayed!");
    }

    @Override
    public void onAdClicked(final MaxAd maxAd) {
        Log.d("Ad Listener", "Inter ad is clicked by a user!");
    }

    @Override
    public void onAdHidden(final MaxAd maxAd)
    {
        // Interstitial ad is hidden. Pre-load the next ad
        interstitialAd.loadAd();
    }
}
