# V Bill
# APT, Lab 1
# 9/10/14

import json
import sys
import re
import urllib 
import cgi
import operator
from bs4 import BeautifulSoup 

# need to return json object with 3 fields
# current stock price, urls of other expiration days, and list of 
# individual contracts sorted in decreasing order of open interest

# to save a webpage, use urllib.urlretrieve('URL'), http://finance.yahoo.com/q/op?s=AAPL
# then open the filename that is returned, use BeautifulSoup to parse to bs4 object
# test cases are already saved files for: f, aapl, xom 

def contractAsJson(filename):
	fhand = open('/home/victoria/Desktop/apt/lab1/scraping-lab/' + filename)            # change later to use urllib?
	soup = BeautifulSoup(fhand) 
	fhand.close()
	
	# find links first, match 'm=201'
	urls_re = re.compile('m=201')
	alinks = soup.find_all('a')
	links = []
	found_dlink = {}
	date_urls = []
	
	for link in alinks:
		links.append(str(link.get('href')))
	
	for x in xrange(len(links)):
		s = urls_re.search(links[x])
		if s:
			found_dlink[x] = s
			if len(links[x]) < 50:
				date_urls.append("http://finance.yahoo.com" + links[x])
	#date_urls = [cgi.escape(unicode(x)) for x in date_urls]
	date_urls = [cgi.escape(x) for x in date_urls]
	print date_urls		
	
	# next, find current stock price, match time_rtq_ticker?
	cp = soup.find_all("span",attrs={"class":"time_rtq_ticker"})
	cp_tag = str(cp[0])
	cp_list = re.split('[<>]',cp_tag)
	current_price = float(cp_list[4])
	print current_price
	
	# finally, find stock options, in table on page
	# for f.dat test, look for F1408 - in links!
	# for yahoo, Apple page, look for AAPL1409, AAPL71409 - in links! use links from above
	year = '14'
	found_options = []
	options_data = {}
	options_quotes = []
	data_name = ['Strike','Symbol','Last','Change','Bid','Ask','Vol','Open']	
	data_name = [unicode(x) for x in data_name]
	data_pos = [6,4,4,6,2,2,2,2]
	file_start = filename.split('.')[0].upper()
	options_re = re.compile(file_start + '7*' + year)	
	for x in xrange(len(links)):
		s = options_re.search(links[x])
		if s:
			found_options.append(alinks[x])
			temp = alinks[x].parent.parent.find_all('td')
			if len(temp) == 8:
				for y in xrange(len(temp)):
					if data_name[y] == unicode('Symbol'):
						temp2 = re.split('[<>]',str(temp[y]))[data_pos[y]]
						data_val = re.search('[A-Z]+7*',temp2).group(0)
						options_data[data_name[y]] = unicode(data_val)
						date = re.search('1'+'[0-9]+',temp2).group(0)
						option_type = re.search('[A-Z]',temp2[5:]).group(0)
					elif data_name[y] == unicode('Change'):
						temp2 = re.split('[<>]',str(temp[y]))
						if len(temp2)>14:
							data_val =' '+  temp2[8]

						else:
							data_val =' '+ temp2[data_pos[y]]
						options_data[data_name[y]] = unicode(data_val)
					else:
						data_val = re.split('[<>]',str(temp[y]))[data_pos[y]]
						options_data[data_name[y]] = unicode(data_val)
				options_data[unicode('Date')] = unicode(date)
				options_data[unicode('Type')] = unicode(option_type)
				if options_data['Last'] == '':
					options_data['Last'] = unicode('N/A')
				options_quotes.append(options_data)
				options_data = {}
			
				#print options_data						
  	# need to sort in decreasing order of open interest
	#options_quotes.sort(key=operator.itemgetter('Open'))
	##### !! this code is from Dr. Aziz's options_hack.py, I could not figure out how to sort
	options_quotes.sort(lambda x, y: 1 if (int(x["Open"].replace(",", "")) < int(y["Open"].replace(",", "" ))) \
                            else -1 if (int(x["Open"].replace(",", "")) > int(y["Open"].replace(",", "" ))) else 0 )
	
	
	jsonQuoteData = json.dumps({"currPrice":current_price, "dateUrls":date_urls, "optionQuotes":options_quotes}, sort_keys=True, indent=4, separators=(',', ': '))
	#print jsonQuoteData
	return jsonQuoteData



# jsonQuoteData = contractAsJson('aapl.dat')

