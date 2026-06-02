num_dims = 9
input_file = "rOpt_detailed_9_10000.txt"
output_file = "CVar2_Convergence_9.pdf"

set terminal pdfcairo size 8.3, 11.7 enhanced font 'Verdana,9'
set output output_file

set datafile commentschars "U"
set datafile separator whitespace

stats input_file using 1 name "FULL" nooutput
max_x = FULL_max

rows = (num_dims > 4) ? ceil(num_dims / 2.0) : num_dims
cols = (num_dims > 4) ? 2 : 1

set multiplot layout rows, cols title sprintf("{/:Bold Vývoj CVar2 pre %d dimenzií}", num_dims) font 'Verdana,14'

set grid lc rgb '#d6d6d6'
set format y "%.3f"
set tmargin 3
set bmargin 3
set lmargin 10
set rmargin 5

set xrange [0:max_x]
set yrange [0:0.4]

array colors[12] = ["#1b9e77", "#d95f02", "#7570b3", "#e7298a", "#66a61e", "#e6ab02", "#a6761d", "#666666", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99"]

do for [d=0:num_dims-2] {

    stats input_file using ($3==d ? $10 : 1/0) name "CHECK" nooutput
    set title sprintf("Dimenzia %d", d)

    unset key

    plot input_file \
         using 1:($3==d ? $10 : 1/0) with steps lc rgb colors[d+1] lw 1.2 title "Local", \
         "" using 1:($3==d ? $11 : 1/0) with lines dt 2 lc rgb 'black' lw 1 title "Exact"
}

unset multiplot
set output