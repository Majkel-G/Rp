num_dims = 9
input_file = "rOpt_detailed_9_1000.txt"

set datafile commentschars "U"
set datafile separator whitespace

stats input_file using 1 name "FULL" nooutput
max_x = FULL_max

set grid lc rgb '#d6d6d6'
set format y "%.3f"
set tmargin 3
set bmargin 3
set lmargin 10
set rmargin 5

set xrange [0:max_x]
set yrange [0:0.4]
set ytics 0, 0.025, 0.4
set mytics 2

array colors[12] = ["#1b9e77", "#d95f02", "#7570b3", "#e7298a", "#66a61e", "#e6ab02", "#a6761d", "#666666", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99"]

do for [d=0:num_dims-2] {
    set yrange [0:0.4]
    out_name = sprintf("CVar22_Dim_%d.pdf", d)

    set terminal pdfcairo size 5, 3.5 enhanced font 'Verdana,10'
    set output out_name

    stats input_file using ($3==d ? $10 : 1/0) name "CHECK" nooutput
    stats input_file using ($3==d ? $11 : 1/0) name "CURRENT_EXACT" nooutput
    val_exact = CURRENT_EXACT_min
    set title sprintf("Vývoj CVar2: Dimenzia %d \t Exact Cvar2: %.4f", d+1, val_exact) font 'Verdana,12,Bold'

    set yrange [val_exact - 0.1 : val_exact + 0.1]
    plot input_file \
         using 1:($3==d ? $10 : 1/0) with steps lc rgb colors[d+1] lw 1.5 title "Local", \
         val_exact with lines dt 1 lc rgb 'black' lw 0.5 title "Exact"

    set output
}