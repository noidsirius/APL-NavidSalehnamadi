#include<iostream>
#include<fstream>
#include<vector>
#include<map>
#include<set>
#include<sstream>
#include<algorithm>
#include<functional>
using namespace std;

using lambda = std::function<void*(void*)>; 
typedef map<string, int> map_str_int ;
typedef vector<pair<string, int> > vec_str_int ;
#define ADDFUNC(OBJ,Y,Z) (OBJ).add_thing(Y,get_lambda_ptr(([&]( BaseObject& me){ return Z;})(OBJ)))
#define ADDDATA(OBJ,Y,Z) (OBJ).add_thing(Y,Z)
#define GETFUNC(OBJ,Y) (*(lambda*)OBJ[Y])
#define GETDATA(OBJ,Y,TYPE) (*(TYPE*)OBJ[Y])
#define UPDDATA(OBJ, Y, Z) (OBJ).update_thing(Y,Z)

const int MAX_LAMBDAS = 100;
lambda *all_lambdas = new lambda[MAX_LAMBDAS];
int last_lambda_ptr;
lambda* get_lambda_ptr(lambda f){
	if (last_lambda_ptr >= MAX_LAMBDAS)
		return NULL;
	all_lambdas[last_lambda_ptr] = f;
	return all_lambdas + last_lambda_ptr++;
}

template<typename Base, typename T>
inline bool instanceof(const T*) {
	return std::is_base_of<Base, T>::value;
}

class BaseObject {
	protected:
		map< string, void*> my_map;
	public:
		BaseObject(){
		}
		void* operator [](string s){
			return my_map[s];
		}
		void add_thing(string s, void* thing){
			if (my_map.find(s) == my_map.end()){
				my_map[s] = thing;
			}
		}
		void update_thing(string s, void* thing){
			my_map[s] = thing;
		}

};
class DataStorageObject : public BaseObject {
	private:
		string content;
		static void refine_text(string &text){
			for(int i=0; i< text.length(); i++)
				if(isalpha(text[i]) || isspace(text[i]) )
					text[i] = tolower(text[i]);
				else
					text[i] = ' ';
		}
	public:
		DataStorageObject(){
			ADDDATA(*this,"content", &content); 
			ADDFUNC(*this, "init", [&](void *file_path_ptr)->void* {
					string file_path = string((char*) file_path_ptr);
					ifstream text_file;
					text_file.open(file_path.c_str());
					if(text_file.fail()){
					cerr <<"This file is not accessible" << endl;
					exit(1);
					}
					string line;
					GETDATA(me, "content", string) = "";
					while(getline(text_file, line))
					GETDATA(me, "content", string) += line + '\n';
					text_file.close();
					refine_text(GETDATA(me, "content", string));
					return NULL;
					});
		} 
} ds_object;

class StopWordObject : public BaseObject{
	private:
		set<string> stop_words;	
		static bool is_blank(string word){
			for(int i=0; i<word.length(); i++)
				if(!isspace(word[i]))
					return false;
			return true;
		}
	public:
		StopWordObject(){
			ADDDATA(*this, "stop_words", &stop_words);
			ADDFUNC(*this, "init", [&](void* empty=NULL)->void*{
					ifstream stop_words_file;
					stop_words_file.open("../stop_words.txt");
					string word;
					while(getline(stop_words_file, word, ',')){
						if(is_blank(word))
							continue;
						GETDATA(me, "stop_words", set<string>).insert(word);
					}
					stop_words_file.close();
					return NULL;
			});
			ADDFUNC(*this, "is_stop_word", [&](void* word_ptr)->void*{
					string word = *(string*)word_ptr;
					set<string> sw = GETDATA(me, "stop_words", set<string>);
					return new bool(is_blank(word) or word.length()<2 or sw.find(word) != sw.end());
				});
		}
} sw_object;

class WordFrequencyObject : public BaseObject{
	private:
		map_str_int frequency_words;
		static bool sortbysecond(const pair<string,int> &a, const pair<string,int> &b) 
		{ 
			return (a.second > b.second); 
		}
		static vector< pair<string, int> >* sorted(map_str_int my_map){
			vector<pair<string, int> > term_frequencies;
			for(map_str_int::iterator it = my_map.begin(); it != my_map.end(); it++)
				term_frequencies.push_back(make_pair(it->first, it->second));
			::sort(term_frequencies.begin(), term_frequencies.end(), [](const pair<string,int> &a, const pair<string,int> &b)->bool{return a.second > b.second;});
			return new vector<pair<string, int> >(term_frequencies);
		}
	public:
		WordFrequencyObject(){
			ADDDATA(*this, "frequency_words", &frequency_words);
			ADDFUNC(*this, "increment_count", [&](void* word_ptr)->void*{
				string word = *(string*) word_ptr;
				map_str_int tmp_map = GETDATA(me, "frequency_words", map_str_int);
					if(tmp_map.find(word) == tmp_map.end())
					GETDATA(me, "frequency_words", map_str_int)[word] = 1;
				else
					GETDATA(me, "frequency_words", map_str_int)[word] += 1;
			return NULL;
			});
			my_map["sorted"] = get_lambda_ptr([&](void* map_ptr)->void*{return sorted(*(map_str_int*)map_ptr);});
			ADDFUNC(*this, "sort", [&](void* empty=NULL)->void*{ return GETFUNC(me,"sorted")(me["frequency_words"]);});
		}
} wf_object;


int main(int argc, char *argv[]){
	if(argc != 2)
	{
		cerr << "Not enough arguments" << endl;
		return 1;
	}
	GETFUNC(ds_object, "init")(argv[1]);
	GETFUNC(sw_object, "init")(NULL);
	string whole_text = GETDATA(ds_object, "content", string);
	stringstream ss(whole_text);
	string word;
	while(ss >> word){
		if(*(bool*)GETFUNC(sw_object, "is_stop_word")(&word))
			continue;
		GETFUNC(wf_object, "increment_count")(&word);
	}
	// Dynamically injecting a function to wf_object
	ADDFUNC(wf_object, "top25", [&](void* empty=NULL)->void* {
		vec_str_int tf = *(vec_str_int *) GETFUNC(me, "sort")(NULL);
		for(int i=0; i<25; i++)
			cout << tf[i].first << "  -   " << tf[i].second << endl;
		return NULL;
	});
	// Calling the added function
	GETFUNC(wf_object, "top25")(NULL);	

	return 0;
}
