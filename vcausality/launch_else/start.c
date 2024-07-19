#pragma comment(lib, "user32.lib")

#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <windows.h>

int main() {
  errno = 0;
  execl("jdk-17.0.2/bin/javaw", "jdk-17.0.2/bin/javaw", "-jar", "shaft_vj1n0t2.jar", NULL);
  if(errno) {
    MessageBox(NULL, "Cannot launch Shaft 1.0.2", "ERROR",MB_ICONERROR);
  }
  return -1;
}
