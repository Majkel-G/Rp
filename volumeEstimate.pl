set terminal pdfcairo size 5,3.5 enhanced font 'Verdana,10'
set output 'rOpt_detailed_9_1000.pdf'

set title "Vývoj odhadu Monte Carlo" font 'Verdana,12,Bold'
set xlabel "Update ID"
set ylabel "Volume"
set grid lc rgb '#d6d6d6' lt 1 lw 0.5
set border 31

set format y "%.2f"
set ytics 0.05

stats "rOpt_detailed_9_1000.txt" using 18 name "EX" nooutput
exact_val = EX_max

set yrange [exact_val-0.5 : exact_val+0.5]
set key top right box opaque

set style line 1 lc rgb 'black' lt 1 lw 1.2
set style line 2 lc rgb '#d95f02' lt 1 lw 1.5
set style line 3 lc rgb '#1b9e77' lt 1 lw 1

label_text = sprintf("Exact Volume: %.4f", exact_val)

updates_per_rep = 9000
num_rep = 10

do for [i=1:num_rep-1] {
    set arrow i from i*updates_per_rep, graph 0 to i*updates_per_rep, graph 1 \
        nohead lc rgb "red" lw 1
}

plot \
    ""
    "rOpt_detailed_9_1000.txt" using 1:12 with lines ls 1 title "Volume Estimate", \
    "rOpt_detailed_9_1000.txt" using 1:18 with lines ls 2 title label_text

set output
