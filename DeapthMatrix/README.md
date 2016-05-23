Usage: FeatureMatching.exe [params] left right dst_path

        -?, -h, --help, --usage (value:true)
                print this message
        --algorithm (value:bm)
                stereo matching method (bm or sgbm)
        --no-display
                don't display results

        left (value:../data/aloeL.jpg)
                left view of the stereopair
        right (value:../data/aloeR.jpg)
                right view of the stereopair
        dst_path (value:None)
                optional path to save the resulting filtered disparity map
