# R version 3.6.1
# RStudio version 1.3.1073
# Author(s): Alfa Yohannis

library(reshape2)
library(plyr)
library(dplyr)
library(extrafont)
library(rstudioapi)

setwd(dirname(rstudioapi::getSourceEditorContext()$path))

fontType <- "serif"
pdfWidth <- 3.54
pdfHeight <- 5

filename <- paste("..", .Platform$file.sep, "largeMessageResults.csv", sep = "")

raw <- read.table(filename, sep = ",", header = TRUE)
ID <- rep(1:5,len=20)
data <- cbind(id=ID, raw)
melted_data <- melt(data, id=c("IsEncrypted", "MessageBytes", "id"))
data_average <- dcast(melted_data, IsEncrypted ~ MessageBytes, mean)
data_sd <- dcast(melted_data, IsEncrypted ~ MessageBytes, sd)


#avg
rownames(data_average) <- data_average[,1]
data_average_matrix <- as.matrix(select(data_average[,-1], 2:4))
data_average_matrix

# top y
lim <- 1.2*max(data_average_matrix[,0:3])

#std
rownames(data_sd) <- data_sd[,1]
data_sd_matrix <- as.matrix(select(data_sd[,-1], 2:4))
data_sd_matrix 

#A function to add arrows on the chart
error.bar <- function(x, y, upper, lower=upper, length=0.1,...){
  arrows(x,y+upper, x, y-lower, angle=90, code=3, length=length, ...)
}

barplot_avg = barplot(data_average_matrix,  col=c("#FFFFFF","#EEEEEE"),beside=T, ylim=c(0,lim))
error.bar(barplot_avg, data_average_matrix, data_sd_matrix)

