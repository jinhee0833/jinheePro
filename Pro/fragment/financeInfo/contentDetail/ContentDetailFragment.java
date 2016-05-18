package com.helloants.helloants.fragment.financeInfo.contentDetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.content.ContentDetailActivity;
import com.helloants.helloants.util.ImageFetcher;
import com.helloants.helloants.util.ImageWorker;
import com.helloants.helloants.util.Utils;

import uk.co.senab.photoview.PhotoView;

public class ContentDetailFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "content_Detail";
    private String mImageUrl;
    private PhotoView mPhotoview;
    private ImageFetcher mImageFetcher;

    public static ContentDetailFragment newInstance(String imageUrl) {
        final ContentDetailFragment f = new ContentDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_content_detail, container, false);
        mPhotoview = (PhotoView) v.findViewById(R.id.photoview_contentdetail);

        return v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ContentDetailActivity.class.isInstance(getActivity())) {
            mImageFetcher = ((ContentDetailActivity) getActivity()).getImageFetcher();
            mImageFetcher.loadImage(mImageUrl, mPhotoview);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (View.OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()) {
            mPhotoview.setOnClickListener((View.OnClickListener) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("사용량", "파괴");
        if (mPhotoview != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mPhotoview);
            mPhotoview.setImageDrawable(null);
        }
    }
}
