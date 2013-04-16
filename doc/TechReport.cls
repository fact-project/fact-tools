\ProvidesClass{TechReport}
\LoadClass[a4paper,12pt]{article}

\usepackage[latin1]{inputenc}
\usepackage[T1]{fontenc}

\usepackage{tikz}
\usepackage{color}
\usepackage{colortbl}
\usepackage{rotating}
\usepackage{booktabs}
\usepackage{helvet}

\usepackage{fix-cm}

\usepackage[pdfborder={0 0 0},hypertexnames=false]{hyperref} 

\makeatletter

\definecolor{tudortmund}{HTML}{82BB26}
\definecolor{darkgray}{RGB}{48,48,48}

% A4 portrait page layout

\topmargin -0.5 in			% Top margin 1 inch

\oddsidemargin 0.0 in			% Left margin 1" on odd-numbered pages
\evensidemargin 0.0 in			% Left margin 1" on even-numbered pages
\marginparwidth 0.75 in			% Width of marginal notes

\textwidth 6.264 in			% Width of text area
\textheight 9.26 in			% Height of text area

\setlength{\parindent}{0mm}
\setlength{\parskip}{2mm}

\newcommand{\changefont}[3]{
\fontfamily{#1} \fontseries{#2} \fontshape{#3}
\selectfont}

\newcommand{\getsfbproject}{XX}
\newcommand{\sfbproject}[1]
{
 \renewcommand{\getsfbproject}{#1}

} 

\def\makesfbtitlepage{

\voffset -2cm
\hoffset -1cm

\thispagestyle{empty}
\enlargethispage{8cm}

\changefont{phv}{m}{n}
\arrayrulecolor{gray}
\arrayrulewidth2pt

\begin{tabular}{ p{4cm} | l }

	\begin{minipage}{3.9cm}
	\vspace{2.3cm}
	\hspace{0.1cm}
		%\begin{center}
			\includegraphics[width=2.6cm]{logos/sfb-logo}
		%\end{center}
	\vspace{2.3cm}

	\end{minipage}
 & 

\hspace{1cm}
\begin{minipage}{11cm}
	\begin{center}
	\Huge \@title
	\end{center}
\end{minipage}
 \\

% \cline{2-2}
	\begin{center}
	\vspace{13cm}

	\begin{rotate}{90}
	\textcolor{darkgray}{\fontsize{50}{60} \selectfont Technical Report}
	\end{rotate}

	\end{center}

 &

\begin{minipage}[t][20.5cm][t]{14cm}

\hspace{1cm}
\begin{minipage}{11cm}
	\begin{center}
	\vspace{1.5cm}
	\makeatletter
	\textcolor{darkgray}{ \LARGE \@author}
	\makeatother
	\end{center}
\end{minipage}

\vfill

\hspace{1cm}
\begin{minipage}{11cm}
	\begin{center}
	  \textcolor{darkgray}{ \Large \@date }
	\end{center}
\end{minipage}

\vfill

\begin{tabular}{ p{6.5cm} p{6.5cm}}
	\begin{flushleft}
	\includegraphics[width=6cm]{logos/tudo-logo} 
	\end{flushleft}
& 
	\begin{flushright}
	\includegraphics[width=6.3cm]{logos/sfb-logo_withText}\\
	\end{flushright}
\end{tabular}

\end{minipage} \\

\end{tabular}

\newpage

\thispagestyle{empty}
\enlargethispage{3cm}
\hoffset 0cm 

\vspace*{2cm}
  Part of the work on this technical report has been supported by Deutsche
  Forschungsgemeinschaft (DFG) within the Collaborative Research
  Center SFB 876 "Providing Information by Resource-Constrained
  Analysis", project \getsfbproject{}.

\vfill

  \begin{tabular}{ll}
    Speaker:&Prof. Dr. Katharina Morik\\
    Address:&TU Dortmund University\\
    &Joseph-von-Fraunhofer-Str. 23\\
    &D-44227 Dortmund\\
    Web:&\href{http://sfb876.tu-dortmund.de}{http://sfb876.tu-dortmund.de}\\
  \end{tabular}

\newpage

\voffset 0cm
\hoffset 0cm

\makeatother
\changefont{cmr}{m}{n}

\setcounter{page}{1}

\arrayrulecolor{black}
\setlength{\arrayrulewidth}{0.4pt}

}