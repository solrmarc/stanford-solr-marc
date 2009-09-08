#!/s/sirsi/Unicorn/Bin/perl
# vufind.pl
# Tim McGeary
# 6 February 2008
#
# Naomi Dushay
#  only do single request to Sirsi xmlitem
#  refactored code 
# 22 September 2008
# 
# This Perl script will take input from a VUFind installation the type of 
# search (single or multiple holdings) and one or more ckeys and it
# will return the xmlitem output from the Unicorn API

# TODO 
# return appropriately (400 error - bad request) if the search value isn't "holding" or "holdings"
# return appropriately (400 error?) if ckey gives bad output  (bad format ckey, unknown ckey)?
# greatly improve the response wrapper
# perhaps allow for command line running?

use CGI;

# set environment variables for Stanford
use lib '/s/sirsi';
use sirsi_env;
get_sirsi_env;
use CGI::Carp qw(fatalsToBrowser);

$query = new CGI;

print $query->header('text/xml');

my $searchType = $query->param('search');
if ($searchType eq "holding") {
	
	# request for call number/item info for a single bib rec
	# (e.g. record view)
	
	# ckey is value of parameter named 'id'
	print get_holdings_xml($query->param('id'));
}

elsif ($searchType eq "holdings") {

	# request for call number / item information for multiple bib recs 
	# (e.g search results)
	
	print get_holdings_xml(paramVals_to_list());

} # end elsif


# get xmlitem response from sirsi, cleaned up
sub get_holdings_xml {
	my $result = "";
	my $ckeys = shift(@_);
	open (API, "echo '$ckeys' | /s/sirsi/Unicorn/Bin/selitem -iC -oI -2N -9 2>/dev/null | /s/sirsi/Unicorn/Bin/xmlitem -ocv -nn -cc 2>/dev/null |");
	while (<API>) {
		$result .= $_;
	}
	close API;

	# remove shelf list number which appears after call number (item_number element)
	$result =~ s/\s{10,}\w+.*\s{100,}\]\]><\/item_number>/\]\]><\/item_number>/g;
	# remove trailing spaces from CDATA value
	$result =~ s/ +(\]\]><\/)/$1/g;
	# remove trailing whitespace from non-CDATA value
	$result =~ s/(\S+)\s+<\//$1<\//g;
	# remove leading whitespace from non-CDATA value
	$result =~ s/>\s+(\S+<\/)/>$1/g;
	# remove newlines
	$result =~ s/\n//g;

	return $result;
}


#  return a string of HTTP parameter values separated by newlines
#  NOTE: exclude parameter key "search"
sub paramVals_to_list {
	my $result = "";
	my @params = $query->param();
	foreach my $key(@params) {
		if ($key ne 'search') {
			# param names id1, id2, ...
			my $val = $query->param($key);  
			$result .= $val . "\n";
		}
	}

	return $result;
}
