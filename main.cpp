#include <opencv2/line_descriptor.hpp>
#include "opencv2/core/utility.hpp"
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/highgui.hpp>

#include "opencv2/core.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/xfeatures2d.hpp"
#include "opencv2/highgui.hpp"

#include <iostream>
#include <fstream>
#include <string>
#define MATCHES_DIST_THRESHOLD 25

using namespace cv;
using namespace cv::line_descriptor;
using namespace cv::xfeatures2d;
using namespace std;


Mat imageMat1;
Mat imageMat2;
vector<KeyPoint> keypoints_1, keypoints_2;
vector<KeyLine> keylines1, keylines2;

static const char* keys =
{ "{@image_path1 | | Image path 1 }"
    "{@image_path2 | | Image path 2 }" };

static void help()
{
  cout << "\nThis example shows the functionalities of lines extraction " << "and descriptors computation furnished by BinaryDescriptor class\n"
            << "Please, run this sample using a command in the form\n" << "./example_line_descriptor_compute_descriptors <path_to_input_image 1>"
            << "<path_to_input_image 2>" << endl;

}

void PoinMatching(){
    //-- Step 1: Detect the keypoints using SURF Detector
    int minHessian = 400;

    Ptr<SURF> detector = SURF::create( minHessian );

    detector->detect( imageMat1, keypoints_1 );
    detector->detect( imageMat2, keypoints_2 );

}

void LineMatching(){
/* create binary masks */
    Mat mask1 = Mat::ones( imageMat1.size(), CV_8UC1 );
    Mat mask2 = Mat::ones( imageMat2.size(), CV_8UC1 );

    /* create a pointer to a BinaryDescriptor object with default parameters */
    Ptr<BinaryDescriptor> bd = BinaryDescriptor::createBinaryDescriptor(  );

    /* compute descriptors */
    Mat descr1, descr2;

    ( *bd )( imageMat1, mask1, keylines1, descr1, false, false );
    ( *bd )( imageMat2, mask2, keylines2, descr2, false, false );

    /* select keylines from first octave and their descriptors */
    vector<KeyLine> lbd_octave1, lbd_octave2;
    Mat left_lbd, right_lbd;
    for ( int i = 0; i < (int) keylines1.size(); i++ )
    {
        if( keylines1[i].octave == 0 )
        {
            lbd_octave1.push_back( keylines1[i] );
            left_lbd.push_back( descr1.row( i ) );
        }
    }

    for ( int j = 0; j < (int) keylines2.size(); j++ )
    {
        if( keylines2[j].octave == 0 )
        {
            lbd_octave2.push_back( keylines2[j] );
            right_lbd.push_back( descr2.row( j ) );
        }
    }

    /* create a BinaryDescriptorMatcher object */
    Ptr<BinaryDescriptorMatcher> bdm = BinaryDescriptorMatcher::createBinaryDescriptorMatcher();

    /* require match */
    vector<DMatch> matches;
    bdm->match( left_lbd, right_lbd, matches );

    /* select best matches */
    vector<DMatch> good_matches;
    for ( int i = 0; i < (int) matches.size(); i++ )
    {
        if( matches[i].distance < MATCHES_DIST_THRESHOLD )
            good_matches.push_back( matches[i] );
    }

    /* create an LSD detector */
    Ptr<LSDDetector> lsd = LSDDetector::createLSDDetector();

    /* detect lines */
    vector<KeyLine> klsd1, klsd2;
    Mat lsd_descr1, lsd_descr2;
    lsd->detect( imageMat1, klsd1, 2, 2, mask1 );
    lsd->detect( imageMat2, klsd2, 2, 2, mask2 );

    /* compute descriptors for lines from first octave */
    bd->compute( imageMat1, klsd1, lsd_descr1 );
    bd->compute( imageMat2, klsd2, lsd_descr2 );

    /* select lines and descriptors from first octave */
    vector<KeyLine> octave0_1, octave0_2;
    Mat leftDEscr, rightDescr;
    for ( int i = 0; i < (int) klsd1.size(); i++ )
    {
        if( klsd1[i].octave == 1 )
        {
            octave0_1.push_back( klsd1[i] );
            leftDEscr.push_back( lsd_descr1.row( i ) );
        }
    }

    for ( int j = 0; j < (int) klsd2.size(); j++ )
    {
        if( klsd2[j].octave == 1 )
        {
            octave0_2.push_back( klsd2[j] );
            rightDescr.push_back( lsd_descr2.row( j ) );
        }
    }

    /* compute matches */
    vector<DMatch> lsd_matches;
    bdm->match( leftDEscr, rightDescr, lsd_matches );

    /* select best matches */
    good_matches.clear();
    for ( int i = 0; i < (int) lsd_matches.size(); i++ )
    {
        if( lsd_matches[i].distance < MATCHES_DIST_THRESHOLD )
            good_matches.push_back( lsd_matches[i] );
    }
}

int main( int argc, char** argv )
{
    /* get parameters from command line */
    CommandLineParser parser( argc, argv, keys );
    String image_path1 = parser.get<String>( 0 );
    String image_path2 = parser.get<String>( 1 );

    if( image_path1.empty() || image_path2.empty() )
    {
        help();
        return -1;
    }

    ofstream fout{};
    fout.open("outputPoint.txt", ios::out);

    /* load image */
    imageMat1 = imread( image_path1, 1 );
    imageMat2 = imread( image_path2, 1 );

    if( imageMat1.data == NULL || imageMat2.data == NULL )
    {
        cout << "Error, images could not be loaded. Please, check their path" << endl;
    }

    PoinMatching();
    LineMatching();

    //-- Write detected  keypoints
    for(size_t i = 0; i <  keypoints_1.size(); i++)
    {
        fout
        << keypoints_1[i].pt.x << "\n" << keypoints_1[i].pt.y << "\n"
        << keypoints_2[i].pt.x << "\n" << keypoints_2[i].pt.y << "\n";
    }
    //-- Write detected  keyLines
    for(size_t i = 0; i <  keylines1.size(); i++)
    {
        fout
        << keylines1[i].pt.x << "\n" << keylines1[i].pt.y << "\n"
        << keylines2[i].pt.x << "\n" << keylines2[i].pt.y << "\n";
    }
    waitKey();
}
