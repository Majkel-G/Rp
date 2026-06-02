num_dims = 9
input_file = "rOpt_detailed_9_1000.txt"

array exact_values[10] = [1.0, 2.0, 3.14159, 4.18879, 4.93480, 5.26379, 5.16771, 4.72477, 4.05871, 3.29851]

set datafile commentschars "U"
set datafile separator whitespace

stats input_file using 1 name "FULL" nooutput
max_x = FULL_max

do for [d=0:num_dims-2] {

    exact_integral = exact_values[d+3]/exact_values[d+2]


    out_name = sprintf("Integral_Dim_%d.pdf", d)
    set terminal pdfcairo size 5, 3.5 enhanced font 'Verdana,10'
    set output out_name

    set title sprintf("Vývoj integrálu: Dimenzia %d\nExact: %.4f", d+1, exact_integral) font 'Verdana,11,Bold'
    set xlabel "Update ID"
    set ylabel "Integral Estimate (Local Mean)"
    set grid lc rgb '#d6d6d6'

    set xrange [0:max_x]

    set yrange [exact_integral -0.2: exact_integral +0.2]

    set ytics autofreq
    set format y "%.2f"

    stats input_file using ($3==d ? $7 : 1/0) name "CHECK" nooutput

    plot input_file \
         using 1:($3==d ? $7 : 1/0) with steps lc rgb "#1b9e77" lw 1.5 title "Local Estimate", \
         exact_integral with lines dt 2 lc rgb 'black' lw 1.2 title "Exact Reference"


    set output
}