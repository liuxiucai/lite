package org.xidea.lite.parser;

import java.util.List;

import org.xidea.lite.Template;

public class TextParser implements Parser {

	public List<Object> parse(Object data) {
		return parse(data, new ParseContextImpl(null));
	}
	public List<Object> parse(Object text,ParseContext context) {
		parseText(context, (String)text, Template.EL_TYPE);
		return context.toResultTree();
	}



	/**
	 * 解析指定文本
	 * 
	 * @public
	 * @abstract
	 * @return <Array> result
	 */
	protected void parseText(ParseContext context,final String text, final int defaultElType) {
		int i = 0;
		int start = 0;
		int length = text.length();
		boolean encode = defaultElType!=Template.EL_TYPE;
		char qute = '"';
		do {
			final int p$ = text.indexOf("${", start);
			if (p$ < 0) {
				continue;
			} else if (p$ > 0 && text.charAt(p$ - 1) == '\\') {
				int pre = p$ - 1;
				while (pre-- > 0 && text.charAt(pre) == '\\')
					;
				int countp1 = p$ - pre;
				context.append(text.substring(start, p$ - countp1 / 2),encode,qute);
				start = p$;
				if ((countp1 & 1) == 0) {// escape
					context.append("${");
					start=p$+2;
					continue;
				}
			}
			String fn = findFN(text, p$);
			// final int p1 = text.indexOf('{', p$);
			if (fn != null) {
				start = parseInstruction(context, text, fn, defaultElType,
						start, p$,encode,qute);
			}

		} while (++i < length);
		if (start < length) {
			context.append(text.substring(start),encode,qute);
		}
	}

	protected String findFN(String text, int p$) {
		int next = p$ + 1;
		for (; next < text.length()
				&& Character.isJavaIdentifierPart(text.charAt(next)); next++)
			;
		String fn = text.substring(p$ + 1, next);
		if ("end".equals(fn) || next < text.length()
				&& text.charAt(next) == '{') {
			return fn;
		} else {
			return null;
		}
	}

	protected int parseInstruction(ParseContext context, String text, String fn,
			int defaultElType, int start, final int p$,boolean encode,char qute){
		if (start < p$) {
			context.append(text.substring(start, p$),encode,qute);
			start = p$;
		}
		if ("end".equals(fn)) {
			context.appendEnd();
			return p$ + fn.length()+1;
		} else {
			int elBegin = p$ + fn.length() + 2;
			int elEnd = findELEnd(text, elBegin);
			if (elEnd > 0) {
				try {
					Object type = fn.length() == 0?defaultElType:fn;
					parseInstruction(context,type,text.substring(elBegin, elEnd));
					return elEnd + 1;
				} catch (Exception e) {
				}
			}
			context.append(text.substring(start, start + 1),encode,qute);
			return start + 1;
		}
	}
	protected void parseInstruction(ParseContext context,Object type,String eltext){
		if(type instanceof Number){
			switch (((Number)type).intValue()) {
			case Template.EL_TYPE:
				Object el = context.optimizeEL(eltext);
				context.appendEL(el);
				return;
			case Template.XML_ATTRIBUTE_TYPE:
				el = context.optimizeEL(eltext);
				context.appendAttribute(null,el);
				return;
			case Template.XML_TEXT_TYPE:
				el = context.optimizeEL(eltext);
				context.appendXmlText(el);
				return;
			default:
				break;
			}
		}
		Object el = context.optimizeEL(eltext);
		context.appendEL(el);
	}

	protected int findELEnd(String text, int elBegin) {
		int length = text.length();
		if (elBegin >= length) {
			return -1;
		}
		int next = elBegin;
		char stringChar = 0;
		int depth = 0;
		do {
			char c = text.charAt(next);
			switch (c) {
			case '\\':
				next++;
				break;
			case '\'':
			case '"':
				if (stringChar == c) {
					stringChar = 0;
				} else if (stringChar == 0) {
					stringChar = c;
				}
				break;
			case '{':
				if (stringChar == 0) {
					depth++;
				}
				break;
			case '}':
				if (stringChar == 0) {
					depth--;
					if (depth < 0) {
						return next;
					}
				}
			}
		} while (++next < length);
		return -1;
	}

}
