#include "opencv2/calib3d.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/core/utility.hpp"
#include "opencv2/ximgproc/disparity_filter.hpp"
#include <iostream>
#include <string>

using namespace cv;
using namespace cv::ximgproc;
using namespace std;

Rect computeROI(Size2i src_sz, Ptr<StereoMatcher> matcher_instance);

const String keys =
    "{help h usage ? |                  | print this message                                                }"
    "{@left          |../data/aloeL.jpg | left view of the stereopair                                       }"
    "{@right         |../data/aloeR.jpg | right view of the stereopair                                      }"
    "{algorithm      |bm                | stereo matching method (bm or sgbm)                               }"
    "{no-display     |                  | don't display results                                             }"
    ;

int main(int argc, char** argv)
{
    CommandLineParser parser(argc,argv,keys);
    parser.about("Disparity Filtering");
    if (parser.has("help"))
    {
        parser.printMessage();
        return 0;
    }
    String left_im = parser.get<String>(0);
    String right_im = parser.get<String>(1);
    String dst_path = "dst_path.png";
    String algo = parser.get<String>("algorithm");
    bool no_display = parser.has("no-display");
    int max_disp = 160;
    double lambda = 8000.0;
    double sigma  = 1.5;

    int wsize;
    if(algo=="sgbm")
        wsize = 3; //default window size for SGBM
    else if(algo=="bm")
        wsize = 7; //default window size for BM on downscaled views (downscaling is performed only for wls_conf)
    else
        wsize = 15; //default window size for BM on full-sized views


    //! [load_views]
    Mat left  = imread(left_im ,IMREAD_COLOR);
    Mat right = imread(right_im,IMREAD_COLOR);
    if ( left.empty() || right.empty())
    {
        cout<<"Cannot read image file";
        return -1;
    }

    Mat left_for_matcher, right_for_matcher;
    Mat left_disp,right_disp;
    Mat filtered_disp;
    Mat conf_map = Mat(left.rows,left.cols,CV_8U);
    conf_map = Scalar(255);
    Rect ROI;
    Ptr<DisparityWLSFilter> wls_filter;
    double matching_time, filtering_time;
    left_for_matcher  = left.clone();
    right_for_matcher = right.clone();

    if(algo=="bm")
    {
        //! [matching]
        Ptr<StereoBM> left_matcher = StereoBM::create(max_disp,wsize);
        wls_filter = createDisparityWLSFilter(left_matcher);
        Ptr<StereoMatcher> right_matcher = createRightMatcher(left_matcher);

        cvtColor(left_for_matcher,  left_for_matcher,  COLOR_BGR2GRAY);
        cvtColor(right_for_matcher, right_for_matcher, COLOR_BGR2GRAY);

        matching_time = (double)getTickCount();
        left_matcher-> compute(left_for_matcher, right_for_matcher,left_disp);
        right_matcher->compute(right_for_matcher,left_for_matcher, right_disp);
        matching_time = ((double)getTickCount() - matching_time)/getTickFrequency();
    }
    else if(algo=="sgbm")
    {
        Ptr<StereoSGBM> left_matcher  = StereoSGBM::create(0,max_disp,wsize);
        left_matcher->setP1(24*wsize*wsize);
        left_matcher->setP2(96*wsize*wsize);
        left_matcher->setPreFilterCap(63);
        left_matcher->setMode(StereoSGBM::MODE_SGBM_3WAY);
        wls_filter = createDisparityWLSFilter(left_matcher);
        Ptr<StereoMatcher> right_matcher = createRightMatcher(left_matcher);

        matching_time = (double)getTickCount();
        left_matcher-> compute(left_for_matcher, right_for_matcher,left_disp);
        right_matcher->compute(right_for_matcher,left_for_matcher, right_disp);
        matching_time = ((double)getTickCount() - matching_time)/getTickFrequency();
    }
    else
    {
        cout<<"Unsupported algorithm";
        return -1;
    }

    //! [filtering]
    wls_filter->setLambda(lambda);
    wls_filter->setSigmaColor(sigma);
    filtering_time = (double)getTickCount();
    wls_filter->filter(left_disp,left,filtered_disp,right_disp);
    filtering_time = ((double)getTickCount() - filtering_time)/getTickFrequency();

    conf_map = wls_filter->getConfidenceMap();

    // Get the ROI that was used in the last filter call:
    ROI = wls_filter->getROI();
    // upscale raw disparity and ROI back for a proper comparison:
    resize(left_disp,left_disp,Size(),2.0,2.0);
    left_disp = left_disp*2.0;
    ROI = Rect(ROI.x*2,ROI.y*2,ROI.width*2,ROI.height*2);
    //collect and print all the stats:
    cout.precision(2);
    cout<<"Matching time:  "<<matching_time<<"s"<<endl;
    cout<<"Filtering time: "<<filtering_time<<"s"<<endl;
    cout<<endl;

    //Save dst_path
    Mat filtered_disp_vis;
    getDisparityVis(filtered_disp,filtered_disp_vis,1);
    imwrite(dst_path,filtered_disp_vis);


    if(!no_display)
    {
        //! [visualization]
        Mat filtered_disp_vis;
        getDisparityVis(filtered_disp,filtered_disp_vis,1);
        namedWindow("filtered disparity", WINDOW_AUTOSIZE);
        imshow("filtered disparity", filtered_disp_vis);
        waitKey();
    }
    return 0;
}

Rect computeROI(Size2i src_sz, Ptr<StereoMatcher> matcher_instance)
{
    int min_disparity = matcher_instance->getMinDisparity();
    int num_disparities = matcher_instance->getNumDisparities();
    int block_size = matcher_instance->getBlockSize();

    int bs2 = block_size/2;
    int minD = min_disparity, maxD = min_disparity + num_disparities - 1;

    int xmin = maxD + bs2;
    int xmax = src_sz.width + minD - bs2;
    int ymin = bs2;
    int ymax = src_sz.height - bs2;

    Rect r(xmin, ymin, xmax - xmin, ymax - ymin);
    return r;
}
