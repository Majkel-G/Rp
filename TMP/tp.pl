set terminal pdf enhanced font 'Verdana, 17'

unset ytics

set output 'mean9_1000_1000.pdf'
set xlabel "P^{(N)}"
set ylabel "value density"
plot "opt_9_1000_1000.txt" using 3 smooth kdensity title ""
set output 'mean9_1000_1000.pdf'
plot [2.8:3.8] NaN lc rgb "white" title "K=8, N=1000*K",\
"uni_9_1000_1000.txt" using 3 smooth kdensity title "uni" dt 4 lw 2 lc rgb "black", \
"opt_9_1000_1000.txt"  using 3 smooth kdensity title "opt" dt 6 lw 2 lc rgb "black", \
"uni_9_1000_1000.txt"  using 2:(0.01):(0):(GPVAL_DATA_Y_MAX) with vectors lc rgb "black" dt 3 nohead title "exact"
#"< tail -1 uni_9_1000_1000.txt" using 4:(0.01):(0):(GPVAL_DATA_Y_MAX) with vectors dt 4 lc rgb "black" nohead notitle, \
#"< tail -1 opt_9_1000_1000.txt" using 4:(0.01):(0):(GPVAL_DATA_Y_MAX) with vectors dt 6 lc rgb "black" nohead notitle

set ytics
set boxwidth 0.5

set output 'dsel9_1000_1000.pdf'
set xtics 1,1,8
set xlabel "\"dimension\" j"
set ylabel "percentage of samples"
set output 'dsel9_1000_1000.pdf'
plot [] [0:1] NaN lc rgb "white" title "K=8, N=1000*K",\
"dsel_9_1000_1000.txt" every ::1 using 1:3 with boxes title "opt\\\_offline" dt 4 lw 2 lc rgb "black",\
"dsel_9_1000_1000.txt" every ::1 using 1:2 with boxes  title "opt" dt 6 lw 2 lc rgb "black"
