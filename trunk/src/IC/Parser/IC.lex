package IC.Parser;

%%

%class Lexer
%public
%function next_token
%type Token
%line
%scanerror LexicalError
%cup

%state COMMENTS
%state BLOCK_COMMENTS

%{
	private boolean comment_flag = false;
	
	public int getLineNumber() { return yyline+1; }
	
	
%}


%eofval{
	if (comment_flag)
    	throw new LexicalError(yytext(), LexicalError.UNFINISHED_COMMENT, yyline);
	return new Token(sym.EOF, yytext(), yyline);
%eofval}

UPR=[A-Z]
LWR=[a-z]
ALPHA=[A-Za-z_]
DIGIT=[0-9]
NUMBER=({DIGIT})+
ALPHA_NUMERIC={ALPHA}|{DIGIT}
NEWLINE = [\n]
ILLEGAL_ID=({DIGIT}|_)({ALPHA_NUMERIC})*
PRINTABLE=[\ -~]
PRINTABLE_NOT_QT=[\ !#-\[\]-~]
STR_CHARS={PRINTABLE_NOT_QT}|\\n|\\t|\\\\|\\\"
UNFINISHED_STRING={QT}({PRINTABLE_NOT_QT})*({NEWLINE})
SQUENT_OF_ZEROES = "0"{DIGIT}+
LARGE_NUM1 = ([2-9])([1-9])([4-9])([7-9])([4-9])([8-9])([3-9])([6-9])([4-9])([8-9])({DIGIT}*)
LARGE_NUM2 = {DIGIT}{DIGIT}{DIGIT}{DIGIT}{DIGIT}{DIGIT}{DIGIT}{DIGIT}{DIGIT}{DIGIT}({DIGIT}+)
LARGE_NUMBER = {LARGE_NUM1}|{LARGE_NUM2}

QT=\"
QUOTE={QT}({STR_CHARS})*{QT}
ILLEGAL_QUOTE={QT}({PRINTABLE_NOT_QT})*{QT}
ID={LWR}({ALPHA_NUMERIC})*
CLS={UPR}({ALPHA_NUMERIC})*
WHITE_SPACE=([\ \n\r\t\f])+

%%

<YYINITIAL> "//" 		{ yybegin(COMMENTS); }
<COMMENTS> [^\n] 		{ }
<COMMENTS> [\n] 		{ yybegin(YYINITIAL); }

<YYINITIAL> "/*" 		{ 
							comment_flag = true;
							yybegin(BLOCK_COMMENTS); 
						}
<BLOCK_COMMENTS> "*/" 	{ 
							comment_flag = false;
							yybegin(YYINITIAL); 
							}
<BLOCK_COMMENTS> .|\n 	{ }

<YYINITIAL> {WHITE_SPACE} { }
<YYINITIAL> "+" 		{ return new Token(sym.PLUS, yytext(), yyline); }
<YYINITIAL> "-" 		{ return new Token(sym.MINUS, yytext(), yyline); }
<YYINITIAL> "*" 		{ return new Token(sym.MULTIPLY, yytext(), yyline); }
<YYINITIAL> "/" 		{ return new Token(sym.DIVIDE, yytext(), yyline); }
<YYINITIAL> "(" 		{ return new Token(sym.LP, yytext(), yyline); }
<YYINITIAL> ")" 		{ return new Token(sym.RP, yytext(), yyline); }
<YYINITIAL> "[" 		{ return new Token(sym.LB, yytext(), yyline); }
<YYINITIAL> "]" 		{ return new Token(sym.RB, yytext(), yyline); }
<YYINITIAL> "{" 		{ return new Token(sym.LCBR, yytext(), yyline); }
<YYINITIAL> "}" 		{ return new Token(sym.RCBR, yytext(), yyline); }
<YYINITIAL> "!"	  		{ return new Token(sym.LNEG, yytext(), yyline); }
<YYINITIAL> "." 		{ return new Token(sym.DOT, yytext(), yyline); }
<YYINITIAL> "," 		{ return new Token(sym.COMMA, yytext(), yyline); }
<YYINITIAL> ">" 		{ return new Token(sym.GT, yytext(), yyline); }
<YYINITIAL> ">=" 		{ return new Token(sym.GTE, yytext(), yyline); }
<YYINITIAL> "<" 		{ return new Token(sym.LT, yytext(), yyline); }
<YYINITIAL> "<=" 		{ return new Token(sym.LTE, yytext(), yyline); }
<YYINITIAL> ";" 		{ return new Token(sym.SEMI, yytext(), yyline); }
<YYINITIAL> "=" 		{ return new Token(sym.ASSIGN, yytext(), yyline); }
<YYINITIAL> "==" 		{ return new Token(sym.EQUAL, yytext(), yyline); }
<YYINITIAL> "&&" 		{ return new Token(sym.LAND, yytext(), yyline); }
<YYINITIAL> "||" 		{ return new Token(sym.LOR, yytext(), yyline); }
<YYINITIAL> "%" 		{ return new Token(sym.MOD, yytext(), yyline); }
<YYINITIAL> "!=" 		{ return new Token(sym.NEQUAL, yytext(), yyline); }
<YYINITIAL> "static"	{ return new Token(sym.STATIC, yytext(), yyline); }
<YYINITIAL> "boolean" 	{ return new Token(sym.BOOLEAN, yytext(), yyline); }
<YYINITIAL> "break" 	{ return new Token(sym.BREAK, yytext(), yyline); }
<YYINITIAL> "class" 	{ return new Token(sym.CLASS, yytext(), yyline); }
<YYINITIAL> "continue" 	{ return new Token(sym.CONTINUE, yytext(), yyline); }
<YYINITIAL> "extends" 	{ return new Token(sym.EXTENDS, yytext(), yyline); }
<YYINITIAL> "else" 		{ return new Token(sym.ELSE, yytext(), yyline); }
<YYINITIAL> "false" 	{ return new Token(sym.FALSE, yytext(), yyline); }
<YYINITIAL> "true"  	{ return new Token(sym.TRUE, yytext(), yyline); }
<YYINITIAL> "if"	  	{ return new Token(sym.IF, yytext(), yyline); }
<YYINITIAL> "int"	  	{ return new Token(sym.INT, yytext(), yyline); }
<YYINITIAL> "new"	  	{ return new Token(sym.NEW, yytext(), yyline); }
<YYINITIAL> "null"	  	{ return new Token(sym.NULL, yytext(), yyline); }
<YYINITIAL> "return"  	{ return new Token(sym.RETURN, yytext(), yyline); }
<YYINITIAL> "this"  	{ return new Token(sym.THIS, yytext(), yyline); }
<YYINITIAL> "while"  	{ return new Token(sym.WHILE, yytext(), yyline); }
<YYINITIAL> "void"  	{ return new Token(sym.VOID, yytext(), yyline); }
<YYINITIAL> "string"  	{ return new Token(sym.STRING, yytext(), yyline); }
<YYINITIAL> "length"  	{ return new Token(sym.LENGTH, yytext(), yyline); }
<YYINITIAL> {SQUENT_OF_ZEROES} { throw new LexicalError(yytext(), LexicalError.SEQUENTIAL_ZEROS, yyline + 1); }
<YYINITIAL> {LARGE_NUMBER} { throw new LexicalError(yytext(), LexicalError.LARGE_NUMBER, yyline + 1); }
<YYINITIAL> {NUMBER} 	{ return new Token(sym.INTEGER, yytext(), yyline); }
<YYINITIAL> {ID}		{ return new Token(sym.ID, yytext(), yyline); }
<YYINITIAL> {CLS}		{ return new Token(sym.CLASS_ID, yytext(), yyline); }
<YYINITIAL> {QUOTE}		{ return new Token(sym.QUOTE, yytext(), yyline); }

<YYINITIAL> {ILLEGAL_ID} { throw new LexicalError(yytext(), LexicalError.ILLEGAL_ID, yyline + 1); }
<YYINITIAL> {UNFINISHED_STRING} { throw new LexicalError(yytext(), LexicalError.UNFINISHED_STRING, yyline + 1); }
<YYINITIAL> {ILLEGAL_QUOTE} { throw new LexicalError(yytext(), LexicalError.ILLEGAL_QUOTE, yyline + 1); }
<YYINITIAL> {PRINTABLE}	{ throw new LexicalError(yytext(), LexicalError.ILLEGAL_CHAR, yyline + 1); }
<YYINITIAL> . 			{ throw new LexicalError(yytext(), LexicalError.GENERAL_ERROR, yyline + 1); }


