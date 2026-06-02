input_file = "rOpt_detailed_9_1000.txt"
output_file = "Global_CVar2_Evolution.pdf"

set terminal pdfcairo size 6, 4 enhanced font 'Verdana,10'
set output output_file

set datafile commentschars "U"
set datafile separator whitespace

set title "Globálny vývoj ProductCVar2" font 'Verdana,12,Bold'
set xlabel "Update ID"
set ylabel "Product CVar2"
set grid lc rgb '#d6d6d6'

stats input_file using 15 name "GCVAR" nooutput

set yrange [0 : 0.2]
set ytics 0, 0.01, 0.2
set mytics 2
set format y "%.2f"

set grid xtics ytics mytics lc rgb '#d6d6d6' lt 1 lw 0.5

plot input_file using 1:15 with lines lc rgb "#7570b3" lw 1.5 title "Product CVar2"

set output