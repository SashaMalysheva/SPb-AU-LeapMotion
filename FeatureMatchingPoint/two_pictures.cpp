#include <opencv2/line_descriptor/descriptor.hpp>

#include "opencv2/core/utility.hpp"
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/highgui.hpp>

#include <iostream>

using namespace cv;
using namespace cv::line_descriptor;
using namespace std;

static const char* keys =
{ "{@image_path | | Image path }" };

static void help()
{
  cout << "\nThis example shows the functionalities of lines extraction " << "furnished by BinaryDescriptor class\n"
       << "Please, run this sample using a command in the form\n" << "./example_line_descriptor_lines_extraction <path_to_input_image>" << endl;
}

int main( int argc, char** argv )
{
  /* get parameters from comand line */
  CommandLineParser parser( argc, argv, keys );
  String image_path = parser.get<String>( 0 );

  if( image_path.empty() )
  {
    help();
    return -1;
  }

  /* load image */
  cv::Mat imageMat = imread( image_path, 1 );
  if( imageMat.data == NULL )
  {
    std::cout << "Error, image could not be loaded. Please, check its path" << std::endl;
    return -1;
  }

  /* create a random binary mask */
  cv::Mat mask = Mat::ones( imageMat.size(), CV_8UC1 );

  /* create a pointer to a BinaryDescriptor object with deafult parameters */
  Ptr<LSDDetector> bd = LSDDetector::createLSDDetector();

  /* create a structure to store extracted lines */
  vector<KeyLine> lines;

  /* extract lines */
  cv::Mat output = imageMat.clone();
  bd->detect( imageMat, lines, 2, 1, mask );

  /* draw lines extracted from octave 0 */
  if( output.channels() == 1 )
    cvtColor( output, output, COLOR_GRAY2BGR );
  for ( size_t i = 0; i < lines.size(); i++ )
  {
    KeyLine kl = lines[i];
    if( kl.octave == 0)
    {
      /* get a random color */
      int R = ( rand() % (int) ( 255 + 1 ) );
      int G = ( rand() % (int) ( 255 + 1 ) );
      int B = ( rand() % (int) ( 255 + 1 ) );

      /* get extremes of line */
      Point pt1 = Point2f( kl.startPointX, kl.startPointY );
      Point pt2 = Point2f( kl.endPointX, kl.endPointY );

      /* draw line */
      line( output, pt1, pt2, Scalar( B, G, R ), 3 );
    }

  }

  /* show lines on image */
  imshow( "LSD lines", output );
  waitKey();
}
