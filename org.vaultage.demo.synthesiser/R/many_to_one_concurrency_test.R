# R version 3.6.1
# RStudio version 1.3.1073
# Author(s): Alfa Yohannis

# Re-run the Java test first using Eclipse before running this code

library(reshape2)
library(plyr)
library(dplyr)
library(extrafont)
library(rstudioapi)

setwd(dirname(rstudioapi::getSourceEditorContext()$path))

font_type <- "serif"
pdf_width <- 3.54
pdf_height <- 5

source_file <- paste("..", .Platform$file.sep, "manyToOneConcurrentTrafficResults.csv", sep = "")
target_file <- paste("graphs", .Platform$file.sep, "manyToOneConcurrentTrafficResults.pdf", sep = "")

# load data from file
raw <- read.table(source_file, sep = ",", header = TRUE)

# adjust values to kilos (K)
raw$NumRequesters <- raw$NumRequesters

# adjust time to seconds (s)
raw$TotalTimeMillis <- raw$TotalTimeMillis / 1000

ID <- rep(1:5,len=80)
data <- cbind(id=ID, raw)
melted_data <- melt(data, id=c("Mode", "NumRequesters", "id"))
data_average <- dcast(melted_data, Mode ~ NumRequesters, mean)
data_sd <- dcast(melted_data, Mode ~ NumRequesters, sd)

#avg
rownames(data_average) <- data_average[,1]
data_average_matrix <- as.matrix(select(data_average[,-1], 2:4))
data_average_matrix

# top y
lim <- 1.3 * max(data_average_matrix[,0:3])

#std
rownames(data_sd) <- data_sd[,1]
data_sd_matrix <- as.matrix(select(data_sd[,-1], 2:4))
data_sd_matrix 

#A function to add arrows on the chart
error.bar <- function(x, y, upper, lower=upper, length=0.1,...){
  arrows(x,y+upper, x, y-lower, angle=90, code=3, length=length, ...)
}

legend_labels <- c("Brokered", "Direct")
legend_colors <- c("black", "black")
legend_bg_colors <- c("white", "lightgray")
legend_columns <- 2
legend_pch <- c(22, 22)

pdf(file = target_file, height <- pdf_height, width <- pdf_width)

par(family = font_type, mar = c(3, 3, 1.5, 0.5), xpd=FALSE)
barplot_avg = barplot(data_average_matrix,  col=c("white","lightgray"),beside=T, ylim=c(0,lim))
grid ( lty = 2, col = "lightgray")
barplot_avg = barplot(data_average_matrix,  col=c("white","lightgray"),beside=T, ylim=c(0,lim), add=TRUE)
error.bar(barplot_avg, data_average_matrix, data_sd_matrix)
legend(
  "top",
#   50, 6,5
  legend_labels,  col=legend_colors,
  pt.bg=legend_bg_colors,
  # fill="white",
  border="black",
  pch=legend_pch,
  bg="white",
  ncol=legend_columns,
  # inset = 0.0,
  # bty="n"
)

title(ylab="Execution Time (s)", line=2, cex.lab=1)
title(xlab="Number of many-to-one concurrent requests", line=2, cex.lab=1)
box()

dev.off()

