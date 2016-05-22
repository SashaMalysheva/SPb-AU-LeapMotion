#include <stdio.h>
#include <iostream>
#include "opencv2/core.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/xfeatures2d.hpp"
#include "opencv2/highgui.hpp"

#include <fstream>
#include <string>

using namespace cv;
using namespace cv::xfeatures2d;
using namespace std;

void readme();

int main( int argc, char** argv )
{
    if( argc != 3 )
    {
        readme();
        return -1;
    }

    Mat img_1 = imread( argv[1], IMREAD_GRAYSCALE );
    Mat img_2 = imread( argv[2], IMREAD_GRAYSCALE );

    if( !img_1.data || !img_2.data )
    {
        cout << " --(!) Error reading images \n";
        return -1;
    }

    //-- Step 1: Detect the keypoints using SURF Detector
    int minHessian = 400;

    Ptr<SURF> detector = SURF::create( minHessian );
    ofstream fout{};
    fout.open("outputPoint.txt", ios::out);
    vector<KeyPoint> keypoints_1, keypoints_2;

    detector->detect( img_1, keypoints_1 );
    detector->detect( img_2, keypoints_2 );


    //-- Write detected  keypoints
    for(size_t i = 0; i <  keypoints_1.size(); i++)
    {
        fout
        << keypoints_1[i].pt.x << "\n" << keypoints_1[i].pt.y << "\n"
        << keypoints_2[i].pt.x << "\n" << keypoints_2[i].pt.y << "\n";
    }
    waitKey(0);

    return 0;
}

void readme()
{
    cout << " Usage: ./SURF_detector <img1> <img2>\n";
}
