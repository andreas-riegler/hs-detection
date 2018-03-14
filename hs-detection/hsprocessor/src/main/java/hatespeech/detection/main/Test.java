package hatespeech.detection.main;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import weka.core.TestInstances;

public class Test implements Comparator<Test>, Comparable<Test>{

	private Integer e;

	private InnerB b = new InnerB();

	public enum TestE{
		PLUM("purple");
		private TestE(String s){this.color = s;};
		public String color;
		public static void go(){}		

	}

	enum Ennumm{
		ASD, ASDD, DFDSF;
		
		enum Enum1212{
			ASDA;
			
			Enum1212() {
				// TODO Auto-generated constructor stub
			}
		}
		
		public Enum1212 enu = Enum1212.ASDA;
	}

	public static class InnerB{
		public static void be(){}
		
		public static class InnerC{
			
		}
	}

	public class InnerOne{
		public class InnerTwo{}
	}

	public static void main(String[] args) {

		Test test1 = new B();
		
		//double wer = 10/0;

		InnerB b = new Test.InnerB();
		
		Test.InnerOne asdasd = test1.new InnerOne();

		Test.InnerOne o1 = test1.new InnerOne();
		Test.InnerOne.InnerTwo o2 = test1.new InnerOne().new InnerTwo();
		
		Test.InnerB.InnerC asdas = new Test.InnerB.InnerC();

		LocalDate d1 = LocalDate.now();
		d1.get(ChronoField.DAY_OF_MONTH);
		
		LongStream.of(1,2,3);
		
		Optional<Integer> opti = Optional.of(5);
		
		List intList = new ArrayList<>();
		intList.add(new Integer(6));
		
		System.console().writer().append(""+5);
		
		System.out.println("same: " + (6 == 6L));
		System.out.println("contains 6L: " + intList.contains(new Integer(6)));
		
		class Innerasd{
			public void roar(){

			}
		}


		//		DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(temporal)

		System.out.println(test1.getX(5));

		//		List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3));
		//		List<Integer> list2 = new CopyOnWriteArrayList<>(Arrays.asList(1,2,3));
		//
		//		for(Integer i : list2){
		//			list2.remove(i);
		//		}
		//
		//		System.out.println(list2.size());
		//
		//		ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
		//
		//		map.put(1, 2);
		//		map.put(2, 3);
		//
		//		int asd = 50;
		//		for(Integer i : map.keySet()){
		//			map.put(asd,asd+1);
		//			asd++;
		//		}
		//
		//		System.out.println(map.size());
		//
		//		IntStream streamStr = IntStream.rangeClosed(1, 3);
		//
		//		List<Integer> list3 = streamStr.mapToObj(i -> i).collect(Collectors.toCollection(ArrayList::new));
		//
		//		Stream<Object> ostream = Stream.of(new Object());
		//
		//		//		list3 = ostream.mapToInt(i -> 1).col
		//
		//		new File("asd").toURI();
		//
		//		//		Paths.get(uri)
		//
		//		Paths.get("asd");
		//
		//		//		Files.copy(Paths.get("asd"), Paths.get("asd"));
		//
		//
		//		Test test = new Test();
		//
		//		boolean booo = test instanceof Comparable<?>;
		//
		//		System.out.println(test instanceof Comparable<?>);
		//
		//		list3.forEach(System.out::println);
		//
		//		OptionalDouble od = null;
		//
		//		AtomicInteger ai = new AtomicInteger(3);
		//
		//		System.out.println(ai);
		//
		//		Function<Integer, String> fu = (Integer i) -> "";
		//
		//		List<Object> names = new ArrayList<Object>();

		//		System.out.println(names instanceof List<Object>);


		//		od.orEls

		//		list3.

		//		list3.add(1);
		//		list3.add("asdasd");
		//		list3.add(.3);
		//		
		//		print(list3);

		//		streamStr.flatMap(p -> Stream.of(p.split("s"))).forEach(System.out::println);

		//		System.out.println(TestE.PLUM);
		//		
		//		Stream<Integer> st = Stream.of(1,2,3);

		//		st.parallel().collect(Collectors.groupingByConcurrent(classifier))

		//		for (int i = 0; i < 5; i++){
		//			System.out.println(i);
		//		}
		//		
		//		
		//		
		//		Consumer<Integer> i = (Integer a) -> {};

		List list4 = new ArrayList<>();
		list4.add("asd");

		Integer x = 10;
		
		Console c = System.console();
		Writer w = c.writer();

		List<Test> listTest = new ArrayList<Test>();
		
		listTest.forEach(i -> System.out.println(i));
		
		Stream<Integer> si = Stream.of(1,2,3);
		
		si.max((a1,b1)-> a1-b1);

		Collections.sort(listTest);

		List<List<?>> list5 = new ArrayList<List<?>>();

		try(Scanner s = new Scanner("")){

		}
		finally{
		}
		

		try{
			throw new FileNotFoundException();
		}
		catch(FileNotFoundException | RuntimeException e){

		}

		try{
			System.out.println("1");
			throw new RuntimeException("try");
		}
		finally{
			System.out.println("finally");
			//throw new RuntimeException("finally");
		}




	}

	//	private static void print(List<String> list){
	//		for(Object o : list){
	//			System.out.println(o);
	//		}
	//	}


	@Override
	public int compareTo(Test o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public final static void asd(){}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	@Override
	protected void finalize(){
	}


	protected int getX(Number n){
		return 3;
	}

	@Override
	public int compare(Test o1, Test o2) {
		// TODO Auto-generated method stub
		return 0;
	}

}

class B extends Test{

	public int getX(Integer n){
		return 5;
	}
}
