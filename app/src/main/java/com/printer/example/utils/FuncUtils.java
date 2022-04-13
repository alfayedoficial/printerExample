package com.printer.example.utils;

/**
 * @author
 *数据转换工具
 */
public class FuncUtils {
	//-------------------------------------------------------
	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
	static public int isOdd(int num)
	{
		return num & 0x1;
	}
	//-------------------------------------------------------
	static public int HexToInt(String inHex)//Hex字符串转int
	{
		return Integer.parseInt(inHex, 16);
	}
	//-------------------------------------------------------
	static public byte HexToByte(String inHex)//Hex字符串转byte
	{
		return (byte) Integer.parseInt(inHex,16);
	}
	//-------------------------------------------------------
	static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
	{
		return String.format("%02x", inByte).toUpperCase();
	}
	//-------------------------------------------------------
	static public String ByteArrToHex(byte[] inBytArr)//字节数组转转hex字符串
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=inBytArr.length;
		for (int i = 0; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	static public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount)//字节数组转转hex字符串，可选长度
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=byteCount;
		for (int i = offset; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	//转hex字符串转字节数组
	static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen)==1)
		{//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}else {//偶数
			result = new byte[(hexlen/2)];
		}
		int j=0;
		for (int i = 0; i < hexlen; i+=2)
		{
			result[j]=HexToByte(inHex.substring(i,i+2));
			j++;
		}
		return result;
	}

	static public String ByteArrToHex2(byte[] inBytArr, int offset, int byteCount)//字节数组转转hex字符串，可选长度
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=byteCount;
		for (int i = offset; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}

	/**
	 * 打印bts的Log
	 * @param bts
	 * @param groupPerCnt 每组个数
	 */
	static public void ByteArrToHexLog(byte[] bts,int groupPerCnt){
		LogUtils.e("data", "===============begin===================");
		System.out.print("===============begin===================");
		StringBuilder s= new StringBuilder("");
		int i = 0;
		int iend=0;
		while (i < bts.length) {
			if (i+groupPerCnt<bts.length)
				iend = i+groupPerCnt;
			else
				iend= bts.length;
			s.append(ByteArrToHex2(bts, i, iend)+"\r\n");
			i += groupPerCnt;
		}
		LogUtils.e("data", s.toString());
		System.out.print(s);
		System.out.print("===============end===================");
		LogUtils.e("data", "===============end===================");

	}
}