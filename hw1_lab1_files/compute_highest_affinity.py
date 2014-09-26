# No need to process files and manipulate strings - we will
# pass in lists (of equal length) that correspond to 
# sites views. The first list is the site visited, the second is
# the user who visited the site.

# See the test cases for more details.

# You must implement this code using nothing but list, dict, set and primitive types.
# For any pair of pages (P,Q) define the affinity to be the number of persons who viewed both.


def highest_affinity(site_list, user_list, time_list):
  # Returned string pair should be ordered by dictionary order
  # I.e., if the highest affinity pair is "foo" and "bar"
  # return ("bar", "foo"). 
  
  test_dict = {}
  for x in site_list:
	if x in test_dict:
		test_dict[x] += 1
	else:
		test_dict[x] = 1
  
  
  sorted_dict = sorted(test_dict, key = test_dict.get)
  pages = (sorted_dict.pop(),sorted_dict.pop())
  out = tuple(sorted(pages))
  
  return (out)
