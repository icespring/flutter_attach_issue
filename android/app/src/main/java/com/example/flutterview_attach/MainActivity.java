package com.example.flutterview_attach;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.platform.PlatformPlugin;

/**
 * A Demo for attach / detach FlutterView from FlutterEngine dynamically
 * you can press detach button to remove FlutterView and press attach button to reattach FlutterView
 * to FlutterEngine again.
 *
 * This demo may crash on Flutter 1.20.x depends on Android Version which is the smallest code to
 * reproduce this issue. But on Flutter 1.17.x or lower version, it works fine.
 */
public class MainActivity extends Activity {

    private FlutterEngine flutterEngine;
    private FlutterView flutterView;
    private PlatformPlugin platformPlugin;

    // button for detach flutter view from flutter engine
    private Button detachButton;
    // button for attach flutter view with flutter engine
    private Button attachButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (flutterEngine == null) {
            setupFlutterEngine();
            setupPlatformPlugin();
        }
        setContentView(getFlutterView());

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (flutterEngine.getDartExecutor().isExecutingDart()) {
            return;
        }
        flutterEngine.getNavigationChannel().setInitialRoute("/");
        DartExecutor.DartEntrypoint entrypoint = DartExecutor.DartEntrypoint.createDefault();
        flutterEngine.getDartExecutor().executeDartEntrypoint(entrypoint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        flutterEngine.getLifecycleChannel().appIsResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flutterEngine.getLifecycleChannel().appIsInactive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        flutterEngine.getLifecycleChannel().appIsPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flutterView.detachFromFlutterEngine();
        if (platformPlugin != null) {
            platformPlugin.destroy();
            platformPlugin = null;
        }
        flutterEngine.getLifecycleChannel().appIsDetached();
        flutterEngine.destroy();
        flutterEngine = null;
    }

    @Override
    public void onBackPressed() {
        flutterEngine.getNavigationChannel().popRoute();
    }

    private View getFlutterView() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        detachButton = new Button(this);
        detachButton.setText("detach");
        detachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flutterView.detachFromFlutterEngine();
                flutterView.setVisibility(View.INVISIBLE);
            }
        });

        attachButton = new Button(this);
        attachButton.setText("attach");
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flutterView.attachToFlutterEngine(flutterEngine);
                flutterView.setVisibility(View.VISIBLE);

            }
        });

        LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(detachButton, buttonParam);
        linearLayout.addView(attachButton, buttonParam);

        flutterView = new FlutterView(this);
        LinearLayout.LayoutParams flutterParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.addView(flutterView, flutterParam);

        flutterView.attachToFlutterEngine(flutterEngine);
        return linearLayout;
    }

    private void setupPlatformPlugin() {
        platformPlugin = new PlatformPlugin(this, flutterEngine.getPlatformChannel());
    }

    void setupFlutterEngine() {
        flutterEngine = new FlutterEngine(this);
    }
}
